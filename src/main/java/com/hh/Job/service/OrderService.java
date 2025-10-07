package com.hh.Job.service;


import com.hh.Job.domain.*;
import com.hh.Job.domain.constant.CartType;
import com.hh.Job.domain.constant.OrderStatus;
import com.hh.Job.domain.request.ReqOrderDTO;
import com.hh.Job.domain.request.ReqUpdateOrderDTO;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.order.ResOrderDTO;
import com.hh.Job.repository.BookRepository;
import com.hh.Job.repository.OrderDetailRepository;
import com.hh.Job.repository.OrderRepository;
import com.hh.Job.repository.UserRepository;
import com.hh.Job.util.SecurityUtil;
import com.hh.Job.util.error.IdInvalidException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    public OrderService(OrderRepository orderRepository,
                        OrderDetailRepository orderDeatailRepository,
                        UserRepository userRepository,
                        BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDeatailRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    // Converter để chuyển đổi giữa Entity và DTO

    public  ResOrderDTO toResOrderDTO(Order order) {
        if (order == null) return null;

        ResOrderDTO dto = new ResOrderDTO();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setOrderType(order.getOrderType() != null ? order.getOrderType().name() : null);
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setAddress(order.getAddress()); // ✅
        dto.setNote(order.getNote());

        // ✅ Tính totalPrice tự động
        dto.setTotalPrice(
                order.getOrderDetails() == null ? 0.0 :
                        order.getOrderDetails().stream()
                                .mapToDouble(d -> {
                                    Double price = d.getBook() != null && d.getBook().getDiscountPrice() != null
                                            ? d.getBook().getDiscountPrice()
                                            : d.getBook() != null ? d.getBook().getPrice() : d.getPrice();
                                    return price * d.getQuantity();
                                })
                                .sum()
        );


        // ✅ Map User
        if (order.getUser() != null) {
            ResOrderDTO.UserOrderDTO userDTO = new ResOrderDTO.UserOrderDTO();
            userDTO.setId(order.getUser().getId());
            userDTO.setName(order.getUser().getName());
            userDTO.setEmail(order.getUser().getEmail());
            dto.setUser(userDTO);
        }

        // ✅ Map OrderDetails
        if (order.getOrderDetails() != null) {
            dto.setOrderDetails(
                    order.getOrderDetails().stream()
                            .map(detail -> toOrderDetailDTO(detail))
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    // Converter để chuyển đổi giữa Entity và DTO

    private  ResOrderDTO.OrderDetailDTO toOrderDetailDTO(OrderDetail detail) {
        ResOrderDTO.OrderDetailDTO dto = new ResOrderDTO.OrderDetailDTO();
        dto.setId(detail.getId());
        dto.setQuantity(detail.getQuantity());
        Double finalPrice = detail.getBook() != null && detail.getBook().getDiscountPrice() != null
                ? detail.getBook().getDiscountPrice()
                : detail.getBook() != null ? detail.getBook().getPrice() : detail.getPrice();
        dto.setPrice(finalPrice);

        // ✅ Map Book
        if (detail.getBook() != null) {
            ResOrderDTO.BookOrderDTO bookDTO = new ResOrderDTO.BookOrderDTO();
            bookDTO.setId(detail.getBook().getId());
            bookDTO.setTitle(detail.getBook().getTitle());
            bookDTO.setPrice(detail.getBook().getPrice());
            bookDTO.setDiscountPrice(detail.getBook().getDiscountPrice());
            dto.setBook(bookDTO);
        }

        return dto;
    }

    // create Order

    public Order createOrder(ReqOrderDTO reqOrderDTO) throws IdInvalidException {
        // 1️⃣ Kiểm tra user hợp lệ
        if (reqOrderDTO.getUserId() == null) {
            throw new IdInvalidException("Thiếu userId");
        }

        User user = userRepository.findById(reqOrderDTO.getUserId())
                .orElseThrow(() -> new IdInvalidException("User không tồn tại"));

        // 2️⃣ Tạo Order entity
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderType(reqOrderDTO.getOrderType() != null ? reqOrderDTO.getOrderType() : CartType.BUY);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setAddress(reqOrderDTO.getAddress());
        order.setNote(reqOrderDTO.getNote());

        // 3️⃣ Xử lý OrderDetails và tính tổng tiền
        List<OrderDetail> orderDetails = new ArrayList<>();
        double totalPrice = 0.0;

        if (reqOrderDTO.getOrderDetails() != null && !reqOrderDTO.getOrderDetails().isEmpty()) {
            for (ReqOrderDTO.OrderDetailRequest detailReq : reqOrderDTO.getOrderDetails()) {
                Book book = bookRepository.findById(detailReq.getBookId())
                        .orElseThrow(() -> new IdInvalidException("Book không tồn tại: " + detailReq.getBookId()));

                OrderDetail detail = new OrderDetail();
                detail.setBook(book);
                detail.setQuantity(detailReq.getQuantity());
                detail.setOrder(order);

                // ✅ Ưu tiên dùng discountPrice nếu có
                double basePrice = (book.getDiscountPrice() != null && book.getDiscountPrice() > 0)
                        ? book.getDiscountPrice()
                        : book.getPrice();

                double price;
                if (order.getOrderType() == CartType.BUY) {
                    price = basePrice; // Mua: dùng giá (ưu tiên discount)
                } else if (order.getOrderType() == CartType.BORROW) {
                    price = basePrice * 0.1; // Mượn: 20% giá sách
                } else {
                    price = basePrice; // fallback
                }

                detail.setPrice(price);
                orderDetails.add(detail);

                // ✅ Cộng tổng
                totalPrice += price * detailReq.getQuantity();
            }
        }

        // 4️⃣ Gán chi tiết và tổng tiền
        order.setOrderDetails(orderDetails);
        order.setTotalPrice(totalPrice);

        // 5️⃣ Lưu order
        return orderRepository.save(order);
    }


    // Get orderById

    public Order getOrderById(Long id) throws IdInvalidException {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Order không tồn tại với ID: " + id));
    }


    // ✅ Hàm convert list
    public List<ResOrderDTO> toResOrderDTOList(List<Order> orders) {
        return orders.stream()
                .map(this::toResOrderDTO)
                .collect(Collectors.toList());
    }


    public ResultPaginationDTO<ResOrderDTO> fetchAllOrder(Specification<Order> spec, Pageable pageable) {
        // Truy vấn có phân trang + filter
        Page<Order> pageOrder = orderRepository.findAll(spec, pageable);

        // Chuyển entity → DTO
        List<ResOrderDTO> orderDTOs = pageOrder.getContent().stream()
                .map(this::toResOrderDTO) // dùng hàm chuyển đổi giống như getAllOrdersByUserId
                .collect(Collectors.toList());

        // Tạo meta pagination
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageOrder.getNumber() + 1); // trả về page theo kiểu người dùng
        meta.setPageSize(pageOrder.getSize());
        meta.setTotal(pageOrder.getTotalElements());
        meta.setPages(pageOrder.getTotalPages());

        // Gộp thành kết quả
        ResultPaginationDTO<ResOrderDTO> result = new ResultPaginationDTO<>();
        result.setMeta(meta);
        result.setResult(orderDTOs);
        return result;
    }


    // Lấy danh sách đơn hàng theo userId, có phân trang + filter
    public ResultPaginationDTO<ResOrderDTO> getAllOrdersByUserId(Long userId, Specification<Order> spec, Pageable pageable)
            throws IdInvalidException {

        // Gộp filter + điều kiện userId
        Specification<Order> finalSpec = (root, query, cb) -> {
            Predicate userPredicate = cb.equal(root.get("user").get("id"), userId);
            if (spec != null) {
                Predicate existingPredicate = spec.toPredicate(root, query, cb);
                return existingPredicate != null ? cb.and(existingPredicate, userPredicate) : userPredicate;
            }
            return userPredicate;
        };

        // Lấy danh sách order có phân trang
        Page<Order> pageOrder = orderRepository.findAll(finalSpec, pageable);

        // Nếu không có đơn nào → ném lỗi
        if (pageOrder.isEmpty()) {
            throw new IdInvalidException("User ID " + userId + " chưa có đơn hàng nào!");
        }

        // Chuyển entity → DTO
        List<ResOrderDTO> orderDTOs = pageOrder.getContent().stream()
                .map(this::toResOrderDTO) // <-- dùng hàm local bên dưới
                .collect(Collectors.toList());

        // Tạo meta pagination
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageOrder.getTotalElements());
        meta.setPages(pageOrder.getTotalPages());

        // Gộp thành kết quả
        ResultPaginationDTO<ResOrderDTO> result = new ResultPaginationDTO<>();
        result.setMeta(meta);
        result.setResult(orderDTOs);
        return result;
    }

    // Xoa order vs order_id

    public void handleDeleteOrder(Long id) throws IdInvalidException {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            throw new IdInvalidException("Không tìm thấy đơn hàng với ID: " + id);
        }

        Order order = orderOptional.get();

        // Xóa các OrderDetail liên quan
        List<OrderDetail> orderDetails = order.getOrderDetails();
        if (orderDetails != null && !orderDetails.isEmpty()) {
            orderDetailRepository.deleteAll(orderDetails);
        }

        // Xóa đơn hàng
        orderRepository.delete(order);
    }

    public void deleteOrdersByUserId(Long userId) throws IdInvalidException {
        // Tìm tất cả đơn hàng của user
        List<Order> orders = orderRepository.findByUserId(userId);

        if (orders.isEmpty()) {
            throw new IdInvalidException("User ID " + userId + " không có đơn hàng nào để xóa.");
        }

        // Xóa các OrderDetail liên quan (nếu không dùng cascade)
        for (Order order : orders) {
            List<OrderDetail> details = order.getOrderDetails(); // nếu có quan hệ mappedBy
            if (details != null && !details.isEmpty()) {
                orderDetailRepository.deleteAll(details);
            }
        }

        // Xóa đơn hàng
        orderRepository.deleteAll(orders);
    }


    private ResOrderDTO convertToResOrderDTO(Order order) {
        ResOrderDTO dto = new ResOrderDTO();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setOrderType(order.getOrderType() != null ? order.getOrderType().name() : null);
        dto.setAddress(order.getAddress());
        dto.setNote(order.getNote());

        // Convert User
        if (order.getUser() != null) {
            ResOrderDTO.UserOrderDTO userDTO = new ResOrderDTO.UserOrderDTO();
            userDTO.setId(order.getUser().getId());
            userDTO.setName(order.getUser().getName());
            userDTO.setEmail(order.getUser().getEmail());
            dto.setUser(userDTO);
        }

        // Convert OrderDetails
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            List<ResOrderDTO.OrderDetailDTO> orderDetailDTOs = order.getOrderDetails().stream()
                    .map(od -> {
                        ResOrderDTO.OrderDetailDTO odDTO = new ResOrderDTO.OrderDetailDTO();
                        odDTO.setId(od.getId());
                        odDTO.setQuantity(od.getQuantity());
                        odDTO.setPrice(od.getPrice());

                        // Convert Book
                        if (od.getBook() != null) {
                            ResOrderDTO.BookOrderDTO bookDTO = new ResOrderDTO.BookOrderDTO();
                            bookDTO.setId(od.getBook().getId());
                            bookDTO.setTitle(od.getBook().getTitle());
                            bookDTO.setPrice(od.getBook().getPrice());
                            bookDTO.setDiscountPrice(od.getBook().getDiscountPrice());

                            odDTO.setBook(bookDTO);
                        }

                        return odDTO;
                    })
                    .collect(Collectors.toList());
            dto.setOrderDetails(orderDetailDTOs);
        }

        return dto;
    }

    public ResOrderDTO handleUpdateOrder(Long id, ReqUpdateOrderDTO reqDto) throws IdInvalidException {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đơn hàng với id = " + id));

        // --- Cập nhật thông tin cơ bản ---
        if (reqDto.getStatus() != null)
            order.setStatus(reqDto.getStatus());
        if (reqDto.getAddress() != null)
            order.setAddress(reqDto.getAddress());
        if (reqDto.getNote() != null)
            order.setNote(reqDto.getNote());
        if (reqDto.getOrderType() != null)
            order.setOrderType(CartType.valueOf(reqDto.getOrderType().toString()));

        // --- Cập nhật chi tiết đơn hàng nếu có ---
        if (reqDto.getOrderDetails() != null && !reqDto.getOrderDetails().isEmpty()) {
            for (ReqUpdateOrderDTO.OrderDetailUpdate detailReq : reqDto.getOrderDetails()) {
                Optional<OrderDetail> optionalDetail = orderDetailRepository.findById(detailReq.getId());
                if (optionalDetail.isPresent()) {
                    OrderDetail detail = optionalDetail.get();

                    if (detailReq.getQuantity() != null)
                        detail.setQuantity(detailReq.getQuantity());

                    Book book = detail.getBook();

                    // ✅ Ưu tiên discountPrice nếu có
                    double basePrice = (book.getDiscountPrice() != null && book.getDiscountPrice() > 0)
                            ? book.getDiscountPrice()
                            : book.getPrice();

                    double price;

                    // ✅ Logic tính tiền tùy theo loại đơn hàng
                    if (order.getOrderType() == CartType.BUY) {
                        price = basePrice; // mua sách: lấy giá bán
                    } else if (order.getOrderType() == CartType.BORROW) {
                        price = basePrice * 0.1; // mượn sách: trả 20%
                    } else {
                        price = basePrice; // mặc định
                    }

                    detail.setPrice(price);
                    detail.setOrder(order);
                    orderDetailRepository.save(detail);
                }
            }
        }

        // --- ✅ Tính lại tổng tiền từ toàn bộ chi tiết đơn hàng ---
        double totalPrice = 0.0;
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        for (OrderDetail d : orderDetails) {
            totalPrice += d.getPrice() * d.getQuantity();
        }

        order.setTotalPrice(totalPrice);
        order.setUpdatedAt(Instant.now());

        Order updated = orderRepository.save(order);
        return convertToResOrderDTO(updated);
    }


}

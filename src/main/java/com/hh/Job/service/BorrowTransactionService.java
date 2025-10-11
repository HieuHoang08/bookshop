package com.hh.Job.service;


import com.hh.Job.domain.*;
import com.hh.Job.domain.constant.BorrowStatus;
import com.hh.Job.domain.constant.CartType;
import com.hh.Job.domain.constant.OrderStatus;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.borrow.BorrowOrderDTO;
import com.hh.Job.domain.response.borrow.ResBorrowDTO;
import com.hh.Job.repository.BookRepository;
import com.hh.Job.repository.BorrowTransactionRepository;
import com.hh.Job.repository.OrderRepository;
import com.hh.Job.util.error.IdInvalidException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BorrowTransactionService {

    private final BorrowTransactionRepository borrowTransactionRepository;
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    public BorrowTransactionService(BorrowTransactionRepository borrowTransactionRepository,
                                    OrderRepository orderRepository,
                                    BookRepository bookRepository) {
        this.borrowTransactionRepository = borrowTransactionRepository;
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
    }

    public ResBorrowDTO findBorrowById(Long id) throws IdInvalidException {
        // 🔍 Tìm BorrowTransaction theo ID
        BorrowTransaction br = borrowTransactionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Borrow transaction id " + id + " not found"));

        // 🚫 Không tính toán lại logic phạt hay hoàn cọc ở đây
        // Chỉ đọc dữ liệu sẵn có để hiển thị

        ResBorrowDTO dto = new ResBorrowDTO();
        dto.setId(br.getId());
        dto.setBorrowDate(br.getBorrowDate());
        dto.setDueDate(br.getDueDate());
        dto.setReturnDate(br.getReturnDate());
        dto.setFine(br.getFine());

        // 💰 Bổ sung thông tin về đặt cọc & hoàn cọc
        dto.setDeposit(br.getDeposit() != null ? br.getDeposit() : BigInteger.ZERO);
        dto.setRefundAmount(br.getRefundAmount() != null ? br.getRefundAmount() : BigInteger.ZERO);
        dto.setDepositRefunded(br.getDepositRefunded() != null ? br.getDepositRefunded() : false);

        if (br.getOrder() != null) {
            dto.setOrderId(br.getOrder().getId());
            dto.setOrderStatus(br.getOrder().getStatus());
            dto.setOrderType(br.getOrder().getOrderType());
            dto.setOrderTotalPrice(br.getOrder().getTotalPrice());
        }

        // ✅ Tính tổng tạm để hiển thị
        double fineValue = br.getFine() != null ? br.getFine().doubleValue() : 0.0;
        double orderPrice = (br.getOrder() != null && br.getOrder().getTotalPrice() != null)
                ? br.getOrder().getTotalPrice() : 0.0;

        dto.setTotalToPay(orderPrice + fineValue);

        return dto;
    }



    public BorrowTransaction findById(Long id) {
        Optional<BorrowTransaction> optionalBorrowTransaction = borrowTransactionRepository.findById(id);
        if (optionalBorrowTransaction.isPresent()) {
            return optionalBorrowTransaction.get();
        }
        return null;

    }

    public ResBorrowDTO createBorrow(BorrowOrderDTO br) throws IdInvalidException {
        if (br.getOrderId() == null) {
            throw new IdInvalidException("Order id is null");
        }

        Order order = orderRepository.findById(br.getOrderId())
                .orElseThrow(() -> new IdInvalidException("Order id not found"));

        // 🟢 Kiểm tra và trừ tồn kho cho từng sách trong order
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Book book = detail.getBook();
                if (book == null) continue;

                int quantityBorrow = detail.getQuantity();
                if (book.getStock() < quantityBorrow) {
                    throw new IllegalStateException(
                            "Sách '" + book.getTitle() + "' không đủ hàng. Còn lại: " + book.getStock()
                    );
                }

                // Trừ tồn kho khi mượn
                book.setStock(book.getStock() - quantityBorrow);
                bookRepository.save(book);
            }
        }

        // 🧾 Tạo giao dịch mượn
        BorrowTransaction transaction = new BorrowTransaction();
        transaction.setOrder(order);
        transaction.setBorrowDate(
                br.getBorrowDate() != null ? br.getBorrowDate() : LocalDate.now()
        );
        transaction.setDueDate(
                br.getDueDate() != null ? br.getDueDate() : LocalDate.now().plusDays(7)
        );
        transaction.setReturnDate(br.getReturnDate());
        transaction.setStatus(BorrowStatus.BORROWED);

        // 💰 Đặt cọc = 20% tổng giá trị đơn hàng
        BigInteger deposit = BigInteger.valueOf(Math.round(order.getTotalPrice() * 0.2));
        transaction.setDeposit(deposit);
        transaction.setDepositRefunded(false);
        transaction.setRefundAmount(BigInteger.ZERO);
        transaction.setFine(BigInteger.ZERO);
        transaction.calculateTotalToPay();

        // 🔄 Cập nhật trạng thái đơn hàng
        order.setStatus(OrderStatus.BORROWED);
        orderRepository.save(order);

        // 💾 Lưu BorrowTransaction
        BorrowTransaction saved = borrowTransactionRepository.save(transaction);

        // 🧭 Map sang DTO trả về
        ResBorrowDTO dto = new ResBorrowDTO();
        dto.setId(saved.getId());
        dto.setBorrowDate(saved.getBorrowDate());
        dto.setDueDate(saved.getDueDate());
        dto.setReturnDate(saved.getReturnDate());
        dto.setFine(saved.getFine());
        dto.setDeposit(saved.getDeposit());
        dto.setRefundAmount(saved.getRefundAmount());
        dto.setDepositRefunded(saved.getDepositRefunded());
        dto.setOrderId(order.getId());
        dto.setOrderStatus(order.getStatus());
        dto.setOrderType(order.getOrderType());
        dto.setOrderTotalPrice(order.getTotalPrice());

        double fineValue = saved.getFine() != null ? saved.getFine().doubleValue() : 0.0;
        dto.setTotalToPay(order.getTotalPrice() + fineValue);

        return dto;
    }




    public ResBorrowDTO updateBorrow(BorrowTransaction borrowTransaction) throws IdInvalidException {
        BorrowTransaction existing = borrowTransactionRepository.findById(borrowTransaction.getId())
                .orElseThrow(() -> new IdInvalidException("Borrow transaction id not found: " + borrowTransaction.getId()));

        // 🧩 Cập nhật thông tin cơ bản
        if (borrowTransaction.getBorrowDate() != null)
            existing.setBorrowDate(borrowTransaction.getBorrowDate());

        if (borrowTransaction.getDueDate() != null)
            existing.setDueDate(borrowTransaction.getDueDate());

        if (borrowTransaction.getReturnDate() != null)
            existing.setReturnDate(borrowTransaction.getReturnDate());

        if (borrowTransaction.getDeposit() != null)
            existing.setDeposit(borrowTransaction.getDeposit());

        // ✅ Nếu có ngày trả → xử lý hoàn kho + cập nhật trạng thái
        if (existing.getReturnDate() != null) {
            Order order = existing.getOrder();

            // 🔁 Hoàn kho sách (nếu chưa hoàn)
            if (order != null && order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    Book book = detail.getBook();
                    if (book != null) {
                        int quantity = detail.getQuantity() != null ? detail.getQuantity() : 0;
                        book.setStock(book.getStock() + quantity); // Cộng lại số lượng sách
                        bookRepository.save(book);
                    }
                }
            }

            // ⚙️ Cập nhật trạng thái giao dịch & đơn hàng
            existing.setStatus(BorrowStatus.RETURNED);
            if (order != null) {
                order.setStatus(OrderStatus.RETURNED);
                orderRepository.save(order);
            }

            // 💰 Tính phạt + hoàn cọc
            existing.calculateFine();
            existing.calculateRefund();
        }

        // 🔄 Tính tổng tiền cuối cùng
        existing.calculateTotalToPay();

        BorrowTransaction updated = borrowTransactionRepository.save(existing);
        return mapToDTO(updated);
    }



    private ResBorrowDTO mapToDTO(BorrowTransaction transaction) {
        ResBorrowDTO dto = new ResBorrowDTO();

        if (transaction == null) return dto;

        // 🆔 Thông tin cơ bản
        dto.setId(transaction.getId());
        dto.setBorrowDate(transaction.getBorrowDate());
        dto.setDueDate(transaction.getDueDate());
        dto.setReturnDate(transaction.getReturnDate());
        dto.setFine(transaction.getFine() != null ? transaction.getFine() : BigInteger.ZERO);

        // 💰 Tiền đặt cọc & hoàn trả
        dto.setDeposit(transaction.getDeposit() != null ? transaction.getDeposit() : BigInteger.ZERO);
        dto.setRefundAmount(transaction.getRefundAmount() != null ? transaction.getRefundAmount() : BigInteger.ZERO);
        dto.setDepositRefunded(transaction.getDepositRefunded() != null && transaction.getDepositRefunded());

        // 📦 Trạng thái mượn / trả
        dto.setBorrowStatus(transaction.getStatus() != null ? transaction.getStatus() : BorrowStatus.BORROWED);

        double fineValue = transaction.getFine() != null ? transaction.getFine().doubleValue() : 0.0;
        double orderPrice = 0.0;

        // 🧾 Thông tin đơn hàng
        if (transaction.getOrder() != null) {
            Order order = transaction.getOrder();
            dto.setOrderId(order.getId());
            dto.setOrderStatus(order.getStatus());
            dto.setOrderType(order.getOrderType());
            dto.setOrderTotalPrice(order.getTotalPrice() != null ? order.getTotalPrice() : 0.0);

            orderPrice = order.getTotalPrice() != null ? order.getTotalPrice() : 0.0;

            // ✅ Nếu là đơn mượn → tính cả tiền phạt
            if (order.getOrderType() == CartType.BORROW) {
                dto.setTotalToPay(orderPrice + fineValue);
            } else {
                dto.setTotalToPay(orderPrice);
            }
        } else {
            // 🔸 Không có order
            dto.setOrderId(null);
            dto.setOrderStatus(null);
            dto.setOrderType(null);
            dto.setOrderTotalPrice(0.0);
            dto.setTotalToPay(fineValue);
        }

        return dto;
    }


    public void deleteBorrow(Long id) {
        Optional<BorrowTransaction> authorOptional = this.borrowTransactionRepository.findById(id);
        if (authorOptional.isPresent()) {
            this.borrowTransactionRepository.deleteById(id);
        }
    }

    public ResultPaginationDTO fetchAllTransactions(Specification<BorrowTransaction> spec, Pageable pageable) {
        Page<BorrowTransaction> page = borrowTransactionRepository.findAll(spec, pageable);

        // 🧮 Meta thông tin phân trang
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());

        // 🧾 Danh sách DTO
        List<ResBorrowDTO> borrowList = page.getContent().stream().map(br -> {
            double fineValue = (br.getFine() != null) ? br.getFine().doubleValue() : 0.0;
            double orderPrice = 0.0;

            Order order = br.getOrder();
            if (order != null && order.getTotalPrice() != null) {
                orderPrice = order.getTotalPrice();
            }

            // ✅ Nếu là đơn mượn thì cộng tiền phạt
            double totalToPay = orderPrice;
            if (order != null && order.getOrderType() == CartType.BORROW) {
                totalToPay += fineValue;
            }

            ResBorrowDTO dto = new ResBorrowDTO();
            dto.setId(br.getId());
            dto.setBorrowDate(br.getBorrowDate());
            dto.setDueDate(br.getDueDate());
            dto.setReturnDate(br.getReturnDate());
            dto.setFine(br.getFine() != null ? br.getFine() : BigInteger.ZERO);
            dto.setDeposit(br.getDeposit() != null ? br.getDeposit() : BigInteger.ZERO);
            dto.setRefundAmount(br.getRefundAmount() != null ? br.getRefundAmount() : BigInteger.ZERO);
            dto.setDepositRefunded(br.getDepositRefunded() != null && br.getDepositRefunded());
            dto.setBorrowStatus(br.getStatus() != null ? br.getStatus() : BorrowStatus.BORROWED);

            if (order != null) {
                dto.setOrderId(order.getId());
                dto.setOrderStatus(order.getStatus());
                dto.setOrderType(order.getOrderType());
                dto.setOrderTotalPrice(orderPrice);
            } else {
                dto.setOrderId(null);
                dto.setOrderStatus(null);
                dto.setOrderType(null);
                dto.setOrderTotalPrice(0.0);
            }

            dto.setTotalToPay(totalToPay);
            return dto;
        }).collect(Collectors.toList());

        // 📦 Kết quả trả về
        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(borrowList);
        return result;
    }


    public ResultPaginationDTO fetchBorrowByUserId(Long userId, Pageable pageable) {
        Page<BorrowTransaction> pageBr = borrowTransactionRepository.findAllByOrderUserId(userId, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageBr.getTotalElements());
        meta.setPages(pageBr.getTotalPages());

        List<ResBorrowDTO> borrowList = pageBr.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(borrowList);
        return result;
    }


    public ResultPaginationDTO fetchOverdueBorrows(Pageable pageable) {
        Page<BorrowTransaction> pageBr = borrowTransactionRepository.findAllOverdue(pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageBr.getTotalElements());
        meta.setPages(pageBr.getTotalPages());

        List<ResBorrowDTO> overdueList = pageBr.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(overdueList);
        return result;
    }

    public ResBorrowDTO returnBorrow(Long id, BorrowTransaction request) throws IdInvalidException {
        BorrowTransaction br = borrowTransactionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Borrow transaction id " + id + " not found"));

        // Ghi nhận ngày trả
        if (request.getReturnDate() != null) {
            br.setReturnDate(request.getReturnDate());
        } else {
            br.setReturnDate(LocalDate.now());
        }

        // ✅ Hoàn kho sách khi trả
        Order order = br.getOrder();
        if (order != null && order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Book book = detail.getBook();
                if (book != null) {
                    int quantity = detail.getQuantity() != null ? detail.getQuantity() : 0;
                    book.setStock(book.getStock() + quantity); // cộng lại số lượng
                    bookRepository.save(book);
                }
            }
        }

        // Cập nhật trạng thái mượn
        br.setStatus(BorrowStatus.RETURNED);

        // Cập nhật trạng thái đơn hàng (nếu có)
        if (order != null) {
            order.setStatus(OrderStatus.RETURNED);
            orderRepository.save(order);
        }

        // Cập nhật tiền phạt + hoàn cọc + tổng tiền
        br.calculateFine();
        br.calculateRefund();
        br.calculateTotalToPay();

        BorrowTransaction saved = borrowTransactionRepository.save(br);
        return mapToDTO(saved);
    }



}

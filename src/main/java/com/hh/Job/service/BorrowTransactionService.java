package com.hh.Job.service;


import com.hh.Job.domain.Author;
import com.hh.Job.domain.Book;
import com.hh.Job.domain.BorrowTransaction;
import com.hh.Job.domain.Order;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.borrow.BorrowOrderDTO;
import com.hh.Job.domain.response.borrow.ResBorrowDTO;
import com.hh.Job.repository.BorrowTransactionRepository;
import com.hh.Job.repository.OrderRepository;
import com.hh.Job.util.error.IdInvalidException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BorrowTransactionService {

    private final BorrowTransactionRepository borrowTransactionRepository;
    private final OrderRepository orderRepository;

    public BorrowTransactionService(BorrowTransactionRepository borrowTransactionRepository,
                                    OrderRepository orderRepository) {
        this.borrowTransactionRepository = borrowTransactionRepository;
        this.orderRepository = orderRepository;
    }

    public ResBorrowDTO findBorrowById(Long id) throws IdInvalidException {
        // Tìm BorrowTransaction theo ID
        BorrowTransaction br = borrowTransactionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Borrow transaction id " + id + " not found"));

        // ✅ KHÔNG gọi calculateFine() hay calculateTotalToPay() ở đây
        // vì ta chỉ đọc dữ liệu, không cập nhật logic kinh doanh

        // Ánh xạ entity -> DTO
        ResBorrowDTO dto = new ResBorrowDTO();
        dto.setId(br.getId());
        dto.setBorrowDate(br.getBorrowDate());
        dto.setDueDate(br.getDueDate());
        dto.setReturnDate(br.getReturnDate());
        dto.setFine(br.getFine());

        if (br.getOrder() != null) {
            dto.setOrderId(br.getOrder().getId());
            dto.setOrderStatus(br.getOrder().getStatus());
            dto.setOrderType(br.getOrder().getOrderType());
            dto.setOrderTotalPrice(br.getOrder().getTotalPrice());
        }

        // ✅ Chỉ tính tạm totalToPay để hiển thị (không lưu vào DB)
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

        Optional<Order> optionalOrder = orderRepository.findById(br.getOrderId());
        if (optionalOrder.isEmpty()) {
            throw new IdInvalidException("Order id not found");
        }

        Order order = optionalOrder.get();

        BorrowTransaction transaction = new BorrowTransaction();
        transaction.setOrder(order);
        transaction.setBorrowDate(br.getBorrowDate());
        transaction.setDueDate(br.getDueDate());
        transaction.setReturnDate(br.getReturnDate());

        BorrowTransaction saved = borrowTransactionRepository.save(transaction);

        // Map entity → ResBorrowDTO
        ResBorrowDTO dto = new ResBorrowDTO();
        dto.setId(saved.getId());
        dto.setBorrowDate(saved.getBorrowDate());
        dto.setDueDate(saved.getDueDate());
        dto.setReturnDate(saved.getReturnDate());
        dto.setFine(saved.getFine());
        dto.setOrderId(order.getId());
        dto.setOrderStatus(order.getStatus());
        dto.setOrderType(order.getOrderType());
        dto.setOrderTotalPrice(order.getTotalPrice());
        double fineValue = saved.getFine() != null ? saved.getFine().doubleValue() : 0.0;
        dto.setTotalToPay(order.getTotalPrice() + fineValue);

        return dto;
    }


    public ResBorrowDTO updateBorrow(BorrowTransaction borrowTransaction) {
        Optional<BorrowTransaction> optional = borrowTransactionRepository.findById(borrowTransaction.getId());
        if (optional.isEmpty()) {
            return null;
        }

        BorrowTransaction existing = optional.get();

        // Cập nhật các field cần thiết
        existing.setBorrowDate(borrowTransaction.getBorrowDate());
        existing.setDueDate(borrowTransaction.getDueDate());
        existing.setReturnDate(borrowTransaction.getReturnDate());

        // Tự động tính lại tiền phạt & tổng tiền cần trả
        existing.calculateFine();
        existing.calculateTotalToPay();

        BorrowTransaction updated = borrowTransactionRepository.save(existing);

        // 👉 Map sang ResBorrowDTO ở Service (để controller không phải làm)
        return mapToDTO(updated);
    }

    private ResBorrowDTO mapToDTO(BorrowTransaction transaction) {
        ResBorrowDTO dto = new ResBorrowDTO();
        dto.setId(transaction.getId());
        dto.setBorrowDate(transaction.getBorrowDate());
        dto.setDueDate(transaction.getDueDate());
        dto.setReturnDate(transaction.getReturnDate());
        dto.setFine(transaction.getFine());

        if (transaction.getOrder() != null) {
            dto.setOrderId(transaction.getOrder().getId());
            dto.setOrderStatus(transaction.getOrder().getStatus());
            dto.setOrderType(transaction.getOrder().getOrderType());
            dto.setOrderTotalPrice(transaction.getOrder().getTotalPrice());

            double fineValue = transaction.getFine() != null ? transaction.getFine().doubleValue() : 0.0;
            double orderPrice = transaction.getOrder().getTotalPrice() != null
                    ? transaction.getOrder().getTotalPrice() : 0.0;
            dto.setTotalToPay(orderPrice + fineValue);
        } else {
            dto.setTotalToPay(transaction.getFine() != null ? transaction.getFine().doubleValue() : 0.0);
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
        Page<BorrowTransaction> pageBr = borrowTransactionRepository.findAll(spec, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageBr.getTotalElements());
        meta.setPages(pageBr.getTotalPages());

        List<ResBorrowDTO> borrowTransactionList = pageBr.getContent()
                .stream()
                .map(br -> {
                    double fineValue = br.getFine() != null ? br.getFine().doubleValue() : 0.0;
                    double orderPrice = (br.getOrder() != null && br.getOrder().getTotalPrice() != null)
                            ? br.getOrder().getTotalPrice() : 0.0;
                    double totalToPay = orderPrice + fineValue;

                    return new ResBorrowDTO(
                            br.getId(),
                            br.getBorrowDate(),
                            br.getDueDate(),
                            br.getReturnDate(),
                            br.getFine(),
                            br.getOrder() != null ? br.getOrder().getId() : null,
                            br.getOrder() != null ? br.getOrder().getStatus() : null,
                            br.getOrder() != null ? br.getOrder().getOrderType() : null,
                            orderPrice,
                            totalToPay
                    );
                })
                .collect(Collectors.toList());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(borrowTransactionList);
        return result;
    }


}

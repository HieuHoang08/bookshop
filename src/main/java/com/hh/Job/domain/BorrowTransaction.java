package com.hh.Job.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hh.Job.domain.constant.BorrowStatus;
import com.hh.Job.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.hh.Job.domain.constant.CartType;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "borrow_transactions")
@Getter
@Setter
public class BorrowTransaction {

    private static final BigInteger FINE_PER_DAY = BigInteger.valueOf(10000); // 10.000đ mỗi ngày trễ
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties(value = {"borrowTransactions", "hibernateLazyInitializer"})
    private Order order;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+7")
    private LocalDate borrowDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+7")
    private LocalDate dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+7")
    private LocalDate returnDate;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private BigInteger fine = BigInteger.ZERO;

    // 💰 Tiền đặt cọc khi mượn
    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private BigInteger deposit = BigInteger.ZERO;

    // 💵 Số tiền thực tế được hoàn lại (deposit - fine)
    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private BigInteger refundAmount = BigInteger.ZERO;

    // ✅ Đánh dấu đã hoàn cọc hay chưa
    private Boolean depositRefunded = false;

    @Enumerated(EnumType.STRING)
    private BorrowStatus status;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+7")
    private Instant createdAt;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+7")
    private Instant updatedAt;

    private String updatedBy;

    @Transient
    private Double totalToPay;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.createdAt = Instant.now();
        if (this.fine == null) {
            this.fine = BigInteger.ZERO;
        }

        // Tính tiền phạt khi tạo (nếu returnDate > dueDate)
        this.calculateFine();
        this.calculateTotalToPay();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.updatedAt = Instant.now();

        // Cập nhật tiền phạt khi returnDate thay đổi
        this.calculateFine();
        this.calculateTotalToPay();
    }

    public void calculateFine() {
        if (this.order != null && this.order.getOrderType() == CartType.BORROW) {
            if (this.returnDate != null && this.dueDate != null && this.returnDate.isAfter(this.dueDate)) {
                long daysLate = Duration.between(
                        this.dueDate.atStartOfDay(),
                        this.returnDate.atStartOfDay()
                ).toDays();

                this.fine = (daysLate > 0)
                        ? FINE_PER_DAY.multiply(BigInteger.valueOf(daysLate))
                        : BigInteger.ZERO;
            } else {
                this.fine = BigInteger.ZERO;
            }
        } else {
            // Nếu không phải đơn BORROW thì không có tiền phạt
            this.fine = BigInteger.ZERO;
        }
    }

    // 🔹 Tính tiền hoàn cọc
    public void calculateRefund() {
        if (this.deposit == null) this.deposit = BigInteger.ZERO;
        if (this.returnDate == null) {
            this.refundAmount = BigInteger.ZERO;
            this.depositRefunded = false;
            return;
        }

        BigInteger refund = this.deposit.subtract(this.fine);
        if (refund.compareTo(BigInteger.ZERO) < 0) refund = BigInteger.ZERO;

        this.refundAmount = refund;
        this.depositRefunded = true;
    }

    public void calculateTotalToPay() {
        double fineValue = (this.fine != null) ? this.fine.doubleValue() : 0.0;
        double orderPrice = 0.0;

        try {
            if (this.order != null && this.order.getTotalPrice() != null) {
                orderPrice = this.order.getTotalPrice();
            }
        } catch (Exception e) {
            // Nếu order là proxy lazy chưa được init, tránh ném exception trong lifecycle hook.
            orderPrice = 0.0;
        }

        this.totalToPay = orderPrice + fineValue;
    }
}

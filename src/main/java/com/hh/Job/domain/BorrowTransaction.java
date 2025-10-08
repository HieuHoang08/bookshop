package com.hh.Job.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hh.Job.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+7")
    private Instant createdAt;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+7")
    private Instant updatedAt;

    private String updatedBy;

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
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.updatedAt = Instant.now();

        // Cập nhật tiền phạt khi returnDate thay đổi
        this.calculateFine();
    }

    private void calculateFine() {
        if (this.returnDate != null && this.dueDate != null && this.returnDate.isAfter(this.dueDate)) {
            long daysLate = Duration.between(this.dueDate.atStartOfDay(), this.returnDate.atStartOfDay()).toDays();
            if (daysLate > 0) {
                this.fine = FINE_PER_DAY.multiply(BigInteger.valueOf(daysLate));
            } else {
                this.fine = BigInteger.ZERO;
            }
        } else {
            this.fine = BigInteger.ZERO;
        }
    }

}

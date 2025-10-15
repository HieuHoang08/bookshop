package com.hh.Job.domain;


import com.hh.Job.domain.constant.DiscountType;
import com.hh.Job.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    @Column(nullable = false)
    private Long discountValue;

    @Column
    private Long minOrderAmount;

    @Column
    private Long maxDiscountAmount;

    @Column
    private Integer quantity;

    @Column
    private Instant startDate;

    @Column
    private Instant endDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    // 👇 Quan hệ ngược lại với VoucherUser
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoucherUser> voucherUsers;

    @Column
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @Column
    private String createdBy;

    @Column
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate () {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() == true ?
                SecurityUtil.getCurrentUserLogin().get() : "";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate () {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent() == true ?
                SecurityUtil.getCurrentUserLogin().get() : "";
        this.updatedAt = Instant.now();
    }
}

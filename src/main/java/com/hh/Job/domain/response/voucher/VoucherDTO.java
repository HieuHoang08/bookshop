package com.hh.Job.domain.response.voucher;

import com.hh.Job.domain.constant.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;


@Getter
@Setter
public class VoucherDTO {
    private Long id;
    private String code;
    private String description;
    private DiscountType discountType;
    private Long discountValue;
    private Long minOrderAmount;
    private Long maxDiscountAmount;
    private Integer quantity;
    private Instant startDate;
    private Instant endDate;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private List<VoucherUserDTO> voucherUsers;

    @Getter
    @Setter
    public static class CreateVoucherDTO {
        private String code;
        private String description;
        private DiscountType discountType; // PERCENT or AMOUNT
        private Long discountValue;
        private Long minOrderAmount;
        private Long maxDiscountAmount;
        private Integer quantity;
        private Instant startDate;
        private Instant endDate;
    }

    @Getter
    @Setter
    public static class UpdateVoucherDTO {
        private String description;
        private Long minOrderAmount;
        private Long maxDiscountAmount;
        private Integer quantity;
        private Instant startDate;
        private Instant endDate;
        private Boolean isActive;
    }

    @Getter
    @Setter
    public static class VoucherUserDTO {
        private Long id;
        private VoucherDTO voucher;
        private Long userId;
        private Long orderId;
        private Instant usedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class ResVoucherDTO {
        private Long id;
        private String code;
        private String description;
        private DiscountType discountType;
        private Long discountValue;
        private Long minOrderAmount;
        private Long maxDiscountAmount;
        private Integer quantity;
        private Integer remainingQuantity;
        private Integer usedCount;
        private Instant startDate;
        private Instant endDate;
        private Boolean isActive;
        private Instant createdAt;
        private Instant updatedAt;
        private String createdBy;
        private String updatedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResVoucherUserDTO {
        private Long id;
        private Long voucherId;
        private String voucherCode;
        private String voucherDescription;
        private DiscountType discountType;
        private Long discountValue;
        private Long minOrderAmount;
        private Long maxDiscountAmount;
        private Long userId;
        private String userName;
        private Long orderId;
        private Instant usedAt;
        private Boolean isUsed;
        private Instant voucherStartDate;
        private Instant voucherEndDate;
        private Boolean isExpired;
    }
}

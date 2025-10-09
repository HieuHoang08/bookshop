package com.hh.Job.domain.response.borrow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hh.Job.domain.constant.CartType;
import com.hh.Job.domain.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResBorrowDTO {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+7")
    private LocalDate borrowDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+7")
    private LocalDate dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+7")
    private LocalDate returnDate;

    private BigInteger fine;

//     Thông tin đơn hàng liên kết
    private Long orderId;
    private OrderStatus orderStatus;
    private CartType orderType;
    private Double orderTotalPrice;

    // ✅ Tổng tiền phải trả = orderTotalPrice + fine
    private Double totalToPay;

}

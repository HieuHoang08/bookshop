package com.hh.Job.domain.request;

import com.hh.Job.domain.constant.CartType;
import com.hh.Job.domain.constant.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReqUpdateOrderDTO {
    private OrderStatus status;
    private CartType orderType;
    private String address;
    private String note;
    private List<OrderDetailUpdate> orderDetails;

    @Getter @Setter
    public static class OrderDetailUpdate {
        private Long id; // ID của OrderDetail cũ (nếu update)
        private Long bookId; // ID của Book
        @NotNull(message = "Quantity không được để trống")
        private Integer quantity;
        private Long price;
        private Long discountPrice;
    }
}

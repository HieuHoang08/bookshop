package com.hh.Job.domain.response.order;

import com.hh.Job.domain.constant.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ResOrderDTO {
    private Long id;
    private OrderStatus status;
    private Double totalPrice;
    private Instant createdAt;
    private Instant updatedAt;
    private UserOrderDTO user;
    private String orderType;
    private String address; // ✅ thêm
    private String note;
    private List<OrderDetailDTO> orderDetails;

    @Getter @Setter
    public static class UserOrderDTO {
        private Long id;
        private String name;
        private String email;
    }

    @Getter @Setter
    public static class OrderDetailDTO {
        private Long id;
        private Integer quantity;
        private Double price;
        private BookOrderDTO book;
    }

    @Getter @Setter
    public static class BookOrderDTO {
        private Long id;
        private String title;
        private Long price;
        private Long discountPrice;
    }
}


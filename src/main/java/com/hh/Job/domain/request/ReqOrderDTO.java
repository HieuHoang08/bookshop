package com.hh.Job.domain.request;

import com.hh.Job.domain.constant.CartType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReqOrderDTO {
    private Long userId;
    private List<OrderDetailRequest> orderDetails;
    private CartType orderType;
    private String address; // ✅ Thêm địa chỉ
    private String note;    // ✅ Thêm ghi chú

    @Getter
    @Setter
    public static class OrderDetailRequest {
        private Long bookId;
        private Integer quantity;
        private Double price; // Optional, có thể tự động lấy từ Book
    }
}

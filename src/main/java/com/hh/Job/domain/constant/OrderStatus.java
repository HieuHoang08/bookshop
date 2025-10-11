package com.hh.Job.domain.constant;

public enum OrderStatus {
        PENDING,        // Đơn hàng mới tạo, chưa xác nhận
        CONFIRMED,      // Đã xác nhận đơn
        SHIPPING,       // Đang giao hàng
        COMPLETED,      // Giao hàng thành công
        CANCELED,       // Đã hủy đơn
        BORROWED,
        RETURNED        // Đã trả sách (nếu là mượn)

}

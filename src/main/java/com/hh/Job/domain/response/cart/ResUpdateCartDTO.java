package com.hh.Job.domain.response.cart;

import com.hh.Job.domain.constant.CartEnum;
import com.hh.Job.domain.constant.CartType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResUpdateCartDTO {
    private Long id;
    private int quantity;
    private CartEnum status;
    private CartType cartType;
    private Instant updatedAt;
    private String updatedBy;

    private Long userId;
    private String userName;
    private Long bookId;
    private String bookTitle;
}


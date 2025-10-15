package com.hh.Job.domain.response.cart;

import com.hh.Job.domain.constant.CartEnum;
import com.hh.Job.domain.constant.CartType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CartDTO {
    private Long id;
    private int quantity;
    private CartEnum status;
    private CartType cartType;

    private Long userId;
    private String userName;

    private List<CartDetailDTO> cartDetails;

    private Instant createdAt;
    private Instant updatedAt;

}

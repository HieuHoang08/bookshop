package com.hh.Job.domain.response.cart;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CartDetailDTO {
    private Long id;
    private int quantity;

    private Long bookId;
    private String bookTitle;
    private Long bookPrice;

    private Instant createdAt;
    private Instant updatedAt;
}


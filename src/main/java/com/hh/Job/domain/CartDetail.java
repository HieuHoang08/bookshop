package com.hh.Job.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hh.Job.util.SecurityUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "cart_details")
@Getter
@Setter
public class CartDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnoreProperties({"carts", "authors", "categories", "publisher"})
    private Book book;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate () {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() == true ?
                SecurityUtil.getCurrentUserLogin().get() : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate () {
        this.updatedBy  = SecurityUtil.getCurrentUserLogin().isPresent() == true ?
                SecurityUtil.getCurrentUserLogin().get() : "";
        this.updatedAt = Instant.now();
    }

}

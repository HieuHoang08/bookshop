package com.hh.Job.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hh.Job.domain.constant.BookStatus;
import com.hh.Job.util.SecurityUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "books",uniqueConstraints = {
        @UniqueConstraint(columnNames = "isbn")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

@Getter
@Setter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Pattern(regexp = "^(97(8|9))?\\d{9}(\\d|X)$", message = "ISBN không hợp lệ")
    @Column(length = 20, unique = true)
    private String isbn;

    private Integer publishYear;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá phải >= 0")
    @Column(nullable = false)
    private Long price;

    @Min(value = 0, message = "Giá khuyến mãi phải >= 0")
    private Long discountPrice;

    @Min(value = 0, message = "Số lượng không được âm")
    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private BookStatus status;


    // Thông tin hệ thống
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    // ManyToMany với Author
    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"books"})
    @JoinTable(name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    private List<Author> authors;

    // ManyToOne với Publisher
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    // ManyToMany với Category
    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"books"})
    @JoinTable(name = "book_category",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = {"book"})
    private List<Cart> carts;

    @PrePersist
    public void handleBeforeCreate () {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() == true ?
                SecurityUtil.getCurrentUserLogin().get() : "";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate () {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent() == true ?
                SecurityUtil.getCurrentUserLogin().get() : "";
        this.updatedAt = Instant.now();
    }
}

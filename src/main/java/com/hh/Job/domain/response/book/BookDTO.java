package com.hh.Job.domain.response.book;


import com.hh.Job.domain.Author;
import com.hh.Job.domain.Category;
import com.hh.Job.domain.Publisher;
import com.hh.Job.domain.constant.BookStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class BookDTO {
    private Long id;
    private String title;
    private String isbn;
    private Integer publishYear;
    private String description;
    private Long price;
    private Long discountPrice;
    private Integer quantity;
    private BookStatus status;
    private Integer stock;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<String> authors;
    private String publisher;
    private List<String> categories;
}

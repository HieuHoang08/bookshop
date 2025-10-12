package com.hh.Job.domain.response.book;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookDetailDTO {
    private Integer numberOfPages;
    private String language;
    private Double weight;
    private String dimensions;
    private String coverImage;
    private String additionalInfo;
}


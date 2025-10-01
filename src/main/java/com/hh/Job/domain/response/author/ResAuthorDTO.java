package com.hh.Job.domain.response.author;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResAuthorDTO {
    private Long id;
    private String name;
    private String dob;
    private String description;
}


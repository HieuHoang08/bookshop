package com.hh.Job.domain.response.category;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResCategoryDTO {
    private Long id;
    private String name;
    private String description;
}

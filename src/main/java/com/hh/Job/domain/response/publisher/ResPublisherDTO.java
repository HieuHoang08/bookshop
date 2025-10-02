package com.hh.Job.domain.response.publisher;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResPublisherDTO {

    private Long id;
    private String name;
    private String address;
    private String email;
    private String phone;

}

package com.hh.Job.domain.response.address;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResAddressDTO {
    private Long id;
    private String street;
    private String ward;
    private String city;
    private String country;
    private String postalCode;
}


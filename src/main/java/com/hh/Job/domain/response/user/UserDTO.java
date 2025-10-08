package com.hh.Job.domain.response.user;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private int age;

    // Thông tin address (gọn nhẹ, không cần object lồng nhau)
    private String street;
    private String ward;
    private String city;
    private String country;
    private String postalCode;


}

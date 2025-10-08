package com.hh.Job.domain.response.user;

import com.hh.Job.domain.Address;
import com.hh.Job.domain.constant.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResUserDTO {

    private long id;
    private String name;
    private String email;
    private GenderEnum gender;
    private Address address;
    private int age;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;

}

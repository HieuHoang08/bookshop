package com.hh.Job.domain.response.user;


import com.hh.Job.domain.Address;
import com.hh.Job.domain.constant.GenderEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResUpdateUserDTO {
    private long id;
    private String name;
    private GenderEnum gender;
    private Address address;
    private int age;
    private String phone;
    private Instant updatedAt;

}

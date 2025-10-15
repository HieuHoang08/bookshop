package com.hh.Job.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hh.Job.util.SecurityUtil;
import com.hh.Job.domain.constant.GenderEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @NotBlank(message = "password khong duoc de trong")
    private String password;
    @NotBlank(message = "email khong duoc de trong")
    private String email;

    @Pattern(regexp="^(\\+84|0)[0-9]{9,10}$", message="Số điện thoại không hợp lệ")
    private String phone;


    private int age;

    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    @JsonIgnoreProperties({"users", "hibernateLazyInitializer", "handler"})
    private Address address;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = "user")
    private List<Order> orders;

    // Quan hệ ngược lại với VoucherUser
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<VoucherUser> voucherUsers;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GTM+7")
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = "user")
    private List<Cart> carts;

    @PrePersist
    public void handleCreatedAt() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent() == true ?
                SecurityUtil.getCurrentUserLogin().get() : "";
        this.createdAt = Instant.now();
    }


}

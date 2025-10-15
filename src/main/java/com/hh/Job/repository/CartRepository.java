package com.hh.Job.repository;

import com.hh.Job.domain.Cart;
import com.hh.Job.domain.User;
import com.hh.Job.domain.constant.CartEnum;
import com.hh.Job.domain.constant.CartType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    Page<Cart> findAll(Specification<Cart> spec, Pageable pageable);

    Optional<Cart> findByUserAndStatusAndCartType(User user, CartEnum cartEnum, CartType cartType);
}

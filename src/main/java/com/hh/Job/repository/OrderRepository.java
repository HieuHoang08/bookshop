package com.hh.Job.repository;

import com.hh.Job.domain.Cart;
import com.hh.Job.domain.Order;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUser_Id(Long userId);

    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    List<Order> findByUserId(Long userId);


}

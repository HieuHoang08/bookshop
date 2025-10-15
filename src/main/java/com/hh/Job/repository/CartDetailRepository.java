package com.hh.Job.repository;


import com.hh.Job.domain.CartDetail;
import com.hh.Job.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long> {
}

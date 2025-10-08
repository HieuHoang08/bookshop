package com.hh.Job.repository;

import com.hh.Job.domain.Address;
import com.hh.Job.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    Page<Address> findAll(Specification<Address> spec, Pageable pageable);
//    List<Address> findByUserId(Long userId);
}

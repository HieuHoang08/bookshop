package com.hh.Job.repository;


import com.hh.Job.domain.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    // Tìm kiếm theo tên
    Optional<Publisher> findByName(String name);

    // Kiểm tra tồn tại theo tên
    boolean existsByName(String name);

    Page<Publisher> findAll(Specification<Publisher> spec, Pageable pageable);

}

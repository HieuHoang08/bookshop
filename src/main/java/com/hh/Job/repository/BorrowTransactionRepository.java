package com.hh.Job.repository;

import com.hh.Job.domain.BorrowTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {

    Page<BorrowTransaction> findAll(Specification<BorrowTransaction> spec, Pageable pageable);
}

package com.hh.Job.repository;

import com.hh.Job.domain.BorrowTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {

    Page<BorrowTransaction> findAll(Specification<BorrowTransaction> spec, Pageable pageable);

    @Query("SELECT b FROM BorrowTransaction b WHERE b.order.user.id = :userId")
    Page<BorrowTransaction> findAllByOrderUserId(@Param("userId") Long userId, Pageable pageable);


    @Query("""
    SELECT b FROM BorrowTransaction b
    WHERE 
        b.order.orderType = 'BORROW'
        AND (
            (b.returnDate IS NOT NULL AND b.returnDate > b.dueDate)
            OR (b.returnDate IS NULL AND b.dueDate < CURRENT_DATE)
        )
""")
    Page<BorrowTransaction> findAllOverdue(Pageable pageable);


}

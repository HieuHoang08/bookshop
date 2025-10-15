package com.hh.Job.repository;


import com.hh.Job.domain.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher,Long> {

    boolean existsByCode(String code);

    Page<Voucher> findAll(Specification spec, Pageable pageable);


//    @Query("SELECT COUNT(b) FROM BorrowTransaction b WHERE b.voucher = :voucher")
//    int countUsedByVoucher(@Param("voucher") Voucher voucher);

    @Query("SELECT COUNT(vu) FROM VoucherUser vu WHERE vu.voucher = :voucher AND vu.usedAt IS NOT NULL")
    int countUsedByVoucher(@Param("voucher") Voucher voucher);

    List<Voucher> findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(Instant start, Instant end);

    Optional<Voucher> findByCodeIgnoreCase(String code);

}


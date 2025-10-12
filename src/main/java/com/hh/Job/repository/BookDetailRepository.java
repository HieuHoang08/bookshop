package com.hh.Job.repository;

import com.hh.Job.domain.BookDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDetailRepository extends JpaRepository<BookDetail,Long> {
    BookDetail findByBookId(Long bookId);
}

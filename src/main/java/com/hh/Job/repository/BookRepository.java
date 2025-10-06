package com.hh.Job.repository;

import com.hh.Job.domain.Book;
import com.hh.Job.domain.constant.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book,Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    Page<Book> findByStatus(BookStatus status, Pageable pageable);

    Page<Book> findAll(Specification<Book> specification, Pageable pageable);
}

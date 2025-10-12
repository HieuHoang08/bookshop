package com.hh.Job.repository;

import com.hh.Job.domain.Book;
import com.hh.Job.domain.constant.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book,Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    Page<Book> findByStatus(BookStatus status, Pageable pageable);

    Page<Book> findAll(Specification<Book> specification, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    Page<Book> findByAuthorName(@Param("authorName") String authorName, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.publisher p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :publisherName, '%'))")
    Page<Book> findByPublisherName(@Param("publisherName") String publisherName, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Book b JOIN b.categories c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))")
    Page<Book> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);


}

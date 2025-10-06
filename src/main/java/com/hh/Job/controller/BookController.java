package com.hh.Job.controller;


import com.hh.Job.domain.Book;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.book.BookDTO;
import com.hh.Job.repository.BookRepository;
import com.hh.Job.service.BookService;
import com.hh.Job.util.annotation.APImessage;
import com.hh.Job.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/v1")
public class BookController {

    private final BookService bookService;
    private final BookRepository bookRepository;

    public BookController(BookService bookService, BookRepository bookRepository) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
    }

    @PostMapping("/books")
    @APImessage("create a book")
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody Book dto)
    throws IdInvalidException {
        if(dto.getIsbn() != null && bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new IdInvalidException("isbn already exists");
        }
        BookDTO book = bookService.createBook(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @GetMapping("/books/{id}")
    @APImessage("fetch book byid")
    public ResponseEntity<BookDTO> getBookById(@PathVariable("id") Long id)
    throws IdInvalidException {
        Optional<Book> books = this.bookService.fetchBookById(id);
        if(!books.isPresent()) {
            throw new IdInvalidException("id not found");
        }

        BookDTO dto = bookService.convertToBookDTO(books.get());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/books")
    @APImessage("update a book")
    public ResponseEntity<BookDTO> update(@Valid @RequestBody Book book) throws IdInvalidException {

        // Kiểm tra ID
        if (book.getId() == null) {
            throw new IdInvalidException("Book ID is required");
        }

        // Kiểm tra tồn tại trong DB
        Optional<Book> existingBook = this.bookService.fetchBookById(book.getId());
        if (!existingBook.isPresent()) {
            throw new IdInvalidException("Book not found with id: " + book.getId());
        }

        // Gọi service update (service đã tự map author/publisher/category)
        BookDTO dto = this.bookService.updateBook(book);

        return ResponseEntity.ok(dto);
    }


    @DeleteMapping("/books/{id}")
    @APImessage("delete a books")
    public ResponseEntity<BookDTO> deleteBookById(@PathVariable("id") Long id)
    throws IdInvalidException {
        Optional<Book> existingBook = this.bookService.fetchBookById(id);
        if(!existingBook.isPresent()) {
            throw new IdInvalidException("Book not found with id: " + id);
        }
        this.bookService.deleteBookById(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/books")
    @APImessage("Get all books")
    public ResponseEntity<ResultPaginationDTO> getAllBooks(@Filter Specification<Book> specification, Pageable pageable) {
        return ResponseEntity.ok().body(this.bookService.fetchAllBooks(specification, pageable));
    }
}

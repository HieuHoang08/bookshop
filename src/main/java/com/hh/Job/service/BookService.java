package com.hh.Job.service;


import com.hh.Job.domain.Author;
import com.hh.Job.domain.Book;
import com.hh.Job.domain.Category;
import com.hh.Job.domain.Publisher;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.book.BookDTO;
import com.hh.Job.repository.AuthorRepository;
import com.hh.Job.repository.BookRepository;
import com.hh.Job.repository.CategoryRepository;
import com.hh.Job.repository.PublisherRepository;
import com.hh.Job.util.annotation.APImessage;
import com.hh.Job.util.error.IdInvalidException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository,
                       CategoryRepository categoryRepository,
                       PublisherRepository publisherRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.publisherRepository = publisherRepository;
    }

    public BookDTO createBook(Book book) {
        if (book.getAuthors() != null) {
            List<Long> authorIds = book.getAuthors()
                    .stream().map(Author::getId)
                    .collect(Collectors.toList());
            List<Author> dbAuthors = authorRepository.findAllById(authorIds);
            book.setAuthors(dbAuthors);
        }

        // Map publisher
        if (book.getPublisher() != null && book.getPublisher().getId() != null) {
            Optional<Publisher> dbPublisher = publisherRepository.findById(book.getPublisher().getId());
            dbPublisher.ifPresent(book::setPublisher);
        }

        // Map categories
        if (book.getCategories() != null) {
            List<Long> categoryIds = book.getCategories()
                    .stream().map(Category::getId)
                    .collect(Collectors.toList());
            List<Category> dbCategories = categoryRepository.findAllById(categoryIds);
            book.setCategories(dbCategories);
        }
        Book savedBook = bookRepository.save(book);

        BookDTO savedBookDTO = new BookDTO();
        savedBookDTO.setId(savedBook.getId());
        savedBookDTO.setTitle(savedBook.getTitle());
        savedBookDTO.setIsbn(savedBook.getIsbn());
        savedBookDTO.setPublisher(savedBook.getPublisher().getName());
        savedBookDTO.setDescription(savedBook.getDescription());
        savedBookDTO.setPrice(savedBook.getPrice());
        savedBookDTO.setDiscountPrice(savedBook.getDiscountPrice());
        savedBookDTO.setQuantity(savedBook.getQuantity());
        savedBookDTO.setQuantity(savedBook.getQuantity());
        savedBookDTO.setStatus(savedBook.getStatus());
        savedBookDTO.setCreatedAt(savedBook.getCreatedAt());
        savedBookDTO.setUpdatedAt(savedBook.getUpdatedAt());
        savedBookDTO.setCreatedBy(savedBook.getCreatedBy());
        savedBookDTO.setUpdatedBy(savedBook.getUpdatedBy());
        return savedBookDTO;

    }

    public Optional<Book> fetchBookById(Long id) {

        return  bookRepository.findById(id);
    }

    public BookDTO convertToBookDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setIsbn(book.getIsbn());
        dto.setPublishYear(book.getPublishYear());
        dto.setDescription(book.getDescription());
        dto.setPrice(book.getPrice());
        dto.setDiscountPrice(book.getDiscountPrice());
        dto.setQuantity(book.getQuantity());
        dto.setStatus(book.getStatus());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        dto.setCreatedBy(book.getCreatedBy());
        dto.setUpdatedBy(book.getUpdatedBy());

        if (book.getAuthors() != null) {
            dto.setAuthors(
                    book.getAuthors().stream()
                            .map(Author::getName)
                            .collect(Collectors.toList())
            );
        }

        if (book.getPublisher() != null) {
            dto.setPublisher(book.getPublisher().getName());
        }

        if (book.getCategories() != null) {
            dto.setCategories(
                    book.getCategories().stream()
                            .map(Category::getName)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public BookDTO updateBook(Book book) {
        // Map Authors nếu có
        if (book.getAuthors() != null) {
            List<Long> authorIds = book.getAuthors()
                    .stream().map(Author::getId)
                    .collect(Collectors.toList());
            List<Author> dbAuthors = authorRepository.findAllById(authorIds);
            book.setAuthors(dbAuthors);
        }

        // Map Publisher nếu có
        if (book.getPublisher() != null && book.getPublisher().getId() != null) {
            publisherRepository.findById(book.getPublisher().getId())
                    .ifPresent(book::setPublisher);
        }

        // Map Categories nếu có
        if (book.getCategories() != null) {
            List<Long> categoryIds = book.getCategories()
                    .stream().map(Category::getId)
                    .collect(Collectors.toList());
            List<Category> dbCategories = categoryRepository.findAllById(categoryIds);
            book.setCategories(dbCategories);
        }

        // Save Book
        Book currentBook = bookRepository.save(book);

        // Build DTO
        BookDTO dto = new BookDTO();
        dto.setId(currentBook.getId());
        dto.setTitle(currentBook.getTitle());
        dto.setIsbn(currentBook.getIsbn());
        dto.setPublishYear(currentBook.getPublishYear());
        dto.setDescription(currentBook.getDescription());
        dto.setPrice(currentBook.getPrice());
        dto.setDiscountPrice(currentBook.getDiscountPrice());
        dto.setQuantity(currentBook.getQuantity());
        dto.setStatus(currentBook.getStatus());
        dto.setCreatedAt(currentBook.getCreatedAt());
        dto.setUpdatedAt(currentBook.getUpdatedAt());
        dto.setCreatedBy(currentBook.getCreatedBy());
        dto.setUpdatedBy(currentBook.getUpdatedBy());

        // Authors
        if (currentBook.getAuthors() != null) {
            List<String> authors = currentBook.getAuthors()
                    .stream().map(Author::getName)
                    .collect(Collectors.toList());
            dto.setAuthors(authors);
        }

        // Publisher
        if (currentBook.getPublisher() != null) {
            dto.setPublisher(currentBook.getPublisher().getName());
        }

        // Categories
        if (currentBook.getCategories() != null) {
            List<String> categories = currentBook.getCategories()
                    .stream().map(Category::getName)
                    .collect(Collectors.toList());
            dto.setCategories(categories);
        }

        return dto;
    }

    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }

    public ResultPaginationDTO fetchAllBooks(Specification<Book> specification, Pageable pageable) {
        Page<Book> pageBook = this.bookRepository.findAll(specification, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageBook.getTotalElements());
        meta.setPages(pageBook.getTotalPages());

        resultPaginationDTO.setMeta(meta);

        // Convert từng Book entity sang BookDTO
        List<BookDTO> bookDTOs = pageBook.getContent().stream()
                .map(this::convertToBookDTO)
                .collect(Collectors.toList());

        resultPaginationDTO.setResult(bookDTOs);

        return resultPaginationDTO;
    }
}

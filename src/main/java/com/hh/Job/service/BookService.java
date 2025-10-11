package com.hh.Job.service;


import com.hh.Job.domain.Author;
import com.hh.Job.domain.Book;
import com.hh.Job.domain.Category;
import com.hh.Job.domain.Publisher;
import com.hh.Job.domain.constant.BookStatus;
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
        // 🧩 Map authors
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            List<Long> authorIds = book.getAuthors()
                    .stream().map(Author::getId)
                    .collect(Collectors.toList());
            List<Author> dbAuthors = authorRepository.findAllById(authorIds);
            book.setAuthors(dbAuthors);
        }

        // 🏢 Map publisher
        if (book.getPublisher() != null && book.getPublisher().getId() != null) {
            publisherRepository.findById(book.getPublisher().getId())
                    .ifPresent(book::setPublisher);
        }

        // 🏷️ Map categories
        if (book.getCategories() != null && !book.getCategories().isEmpty()) {
            List<Long> categoryIds = book.getCategories()
                    .stream().map(Category::getId)
                    .collect(Collectors.toList());
            List<Category> dbCategories = categoryRepository.findAllById(categoryIds);
            book.setCategories(dbCategories);
        }

        // ⚙️ Thiết lập mặc định
        if (book.getStatus() == null) {
            book.setStatus(BookStatus.AVAILABLE); // hoặc BookStatus.ACTIVE nếu bạn có enum này
        }

        // ✅ Gán stock = quantity khi tạo mới
        if (book.getStock() == null || book.getStock() == 0) {
            book.setStock(book.getQuantity());
        }

        // 💾 Lưu sách
        Book savedBook = bookRepository.save(book);

        // 🧭 Map sang DTO trả về
        BookDTO dto = new BookDTO();
        dto.setId(savedBook.getId());
        dto.setTitle(savedBook.getTitle());
        dto.setIsbn(savedBook.getIsbn());
        dto.setDescription(savedBook.getDescription());
        dto.setPrice(savedBook.getPrice());
        dto.setDiscountPrice(savedBook.getDiscountPrice());
        dto.setQuantity(savedBook.getQuantity());
        dto.setStock(savedBook.getStock());
        dto.setStatus(savedBook.getStatus());
        dto.setCreatedAt(savedBook.getCreatedAt());
        dto.setUpdatedAt(savedBook.getUpdatedAt());
        dto.setCreatedBy(savedBook.getCreatedBy());
        dto.setUpdatedBy(savedBook.getUpdatedBy());

        // 🏢 Publisher name (nếu có)
        dto.setPublisher(savedBook.getPublisher() != null ? savedBook.getPublisher().getName() : null);

        return dto;
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
        dto.setStock(book.getStock());
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
        // 🔍 Kiểm tra tồn tại
        Book existing = bookRepository.findById(book.getId())
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + book.getId()));

        // Cập nhật các trường cơ bản
        existing.setTitle(book.getTitle());
        existing.setIsbn(book.getIsbn());
        existing.setPublishYear(book.getPublishYear());
        existing.setDescription(book.getDescription());
        existing.setPrice(book.getPrice());
        existing.setDiscountPrice(book.getDiscountPrice());
        existing.setQuantity(book.getQuantity());
        existing.setStock(book.getStock());
        existing.setStatus(book.getStatus());

        // Map Authors nếu có
        if (book.getAuthors() != null) {
            List<Long> authorIds = book.getAuthors()
                    .stream().map(Author::getId)
                    .collect(Collectors.toList());
            existing.setAuthors(authorRepository.findAllById(authorIds));
        }

        // Map Publisher nếu có
        if (book.getPublisher() != null && book.getPublisher().getId() != null) {
            publisherRepository.findById(book.getPublisher().getId())
                    .ifPresent(existing::setPublisher);
        }

        // Map Categories nếu có
        if (book.getCategories() != null) {
            List<Long> categoryIds = book.getCategories()
                    .stream().map(Category::getId)
                    .collect(Collectors.toList());
            existing.setCategories(categoryRepository.findAllById(categoryIds));
        }

        // ✅ Save lại
        Book updated = bookRepository.save(existing);

        // Convert to DTO
        BookDTO dto = new BookDTO();
        dto.setId(updated.getId());
        dto.setTitle(updated.getTitle());
        dto.setIsbn(updated.getIsbn());
        dto.setPublishYear(updated.getPublishYear());
        dto.setDescription(updated.getDescription());
        dto.setPrice(updated.getPrice());
        dto.setDiscountPrice(updated.getDiscountPrice());
        dto.setQuantity(updated.getQuantity());
        dto.setStock(updated.getStock());
        dto.setStatus(updated.getStatus());
        dto.setCreatedAt(updated.getCreatedAt());
        dto.setUpdatedAt(updated.getUpdatedAt());
        dto.setCreatedBy(updated.getCreatedBy());
        dto.setUpdatedBy(updated.getUpdatedBy());
        dto.setPublisher(updated.getPublisher() != null ? updated.getPublisher().getName() : null);
        dto.setAuthors(updated.getAuthors() != null ? updated.getAuthors().stream().map(Author::getName).collect(Collectors.toList()) : null);
        dto.setCategories(updated.getCategories() != null ? updated.getCategories().stream().map(Category::getName).collect(Collectors.toList()) : null);

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

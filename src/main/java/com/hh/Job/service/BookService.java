package com.hh.Job.service;


import com.hh.Job.domain.Author;
import com.hh.Job.domain.Book;
import com.hh.Job.domain.BookDetail;
import com.hh.Job.domain.Category;
import com.hh.Job.domain.constant.BookStatus;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.book.BookDTO;
import com.hh.Job.domain.response.book.BookDetailDTO;
import com.hh.Job.repository.AuthorRepository;
import com.hh.Job.repository.BookRepository;
import com.hh.Job.repository.CategoryRepository;
import com.hh.Job.repository.PublisherRepository;
import com.hh.Job.util.error.IdInvalidException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
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
        // Map Authors
        if (book.getAuthors() != null) {
            List<Long> authorIds = book.getAuthors()
                    .stream().map(Author::getId)
                    .collect(Collectors.toList());
            List<Author> dbAuthors = authorRepository.findAllById(authorIds);
            book.setAuthors(dbAuthors);
        }

        // Map Publisher
        if (book.getPublisher() != null && book.getPublisher().getId() != null) {
            publisherRepository.findById(book.getPublisher().getId())
                    .ifPresent(book::setPublisher);
        }

        // Map Categories
        if (book.getCategories() != null) {
            List<Long> categoryIds = book.getCategories()
                    .stream().map(Category::getId)
                    .collect(Collectors.toList());
            List<Category> dbCategories = categoryRepository.findAllById(categoryIds);
            book.setCategories(dbCategories);
        }

        // --- Xử lý BookDetail ---
        if (book.getBookDetail() != null) {
            BookDetail detail = book.getBookDetail();
            detail.setBook(book); // set liên kết 2 chiều
            book.setBookDetail(detail);
        }

        // Save Book
        Book savedBook = bookRepository.save(book);

        // Convert sang DTO
        BookDTO dto = new BookDTO();
        dto.setId(savedBook.getId());
        dto.setTitle(savedBook.getTitle());
        dto.setIsbn(savedBook.getIsbn());
        dto.setPublishYear(savedBook.getPublishYear());
        dto.setDescription(savedBook.getDescription());
        dto.setPrice(savedBook.getPrice());
        dto.setDiscountPrice(savedBook.getDiscountPrice());
        dto.setQuantity(savedBook.getQuantity());
        dto.setStatus(savedBook.getStatus());
        dto.setCreatedAt(savedBook.getCreatedAt());
        dto.setUpdatedAt(savedBook.getUpdatedAt());
        dto.setCreatedBy(savedBook.getCreatedBy());
        dto.setUpdatedBy(savedBook.getUpdatedBy());

        // Authors
        if (savedBook.getAuthors() != null) {
            List<String> authors = savedBook.getAuthors()
                    .stream().map(Author::getName)
                    .collect(Collectors.toList());
            dto.setAuthors(authors);
        }

        // Publisher
        if (savedBook.getPublisher() != null) {
            dto.setPublisher(savedBook.getPublisher().getName());
        }

        // Categories
        if (savedBook.getCategories() != null) {
            List<String> categories = savedBook.getCategories()
                    .stream().map(Category::getName)
                    .collect(Collectors.toList());
            dto.setCategories(categories);
        }

        // BookDetail
        if (savedBook.getBookDetail() != null) {
            BookDetail detail = savedBook.getBookDetail();
            BookDetailDTO detailDTO = new BookDetailDTO();
            detailDTO.setNumberOfPages(detail.getNumberOfPages());
            detailDTO.setLanguage(detail.getLanguage());
            detailDTO.setWeight(detail.getWeight());
            detailDTO.setDimensions(detail.getDimensions());
            detailDTO.setCoverImage(detail.getCoverImage());
            detailDTO.setAdditionalInfo(detail.getAdditionalInfo());
            dto.setBookDetail(detailDTO);
        }

        return dto;
    }



    public Optional<Book> fetchBookById(Long id) {

        return  bookRepository.findById(id);
    }

    public BookDTO convertToBookDTO(Book book) {
        if (book == null) {
            return null;
        }

        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setIsbn(book.getIsbn());
        dto.setPublishYear(book.getPublishYear());
        dto.setDescription(book.getDescription());
        dto.setPrice(book.getPrice());
        dto.setDiscountPrice(book.getDiscountPrice());
        dto.setQuantity(book.getQuantity());
        dto.setStock(book.getStock());
        dto.setStatus(book.getStatus());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        dto.setCreatedBy(book.getCreatedBy());
        dto.setUpdatedBy(book.getUpdatedBy());

        // 🔹 Authors
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            List<String> authorNames = book.getAuthors()
                    .stream()
                    .map(Author::getName)
                    .collect(Collectors.toList());
            dto.setAuthors(authorNames);
        }

        // 🔹 Publisher
        if (book.getPublisher() != null) {
            dto.setPublisher(book.getPublisher().getName());
        }

        // 🔹 Categories
        if (book.getCategories() != null && !book.getCategories().isEmpty()) {
            List<String> categoryNames = book.getCategories()
                    .stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
            dto.setCategories(categoryNames);
        }

        // 🔹 BookDetail
        if (book.getBookDetail() != null) {
            BookDetail detail = book.getBookDetail();
            BookDetailDTO detailDTO = new BookDetailDTO();
            detailDTO.setNumberOfPages(detail.getNumberOfPages());
            detailDTO.setLanguage(detail.getLanguage());
            detailDTO.setWeight(detail.getWeight());
            detailDTO.setDimensions(detail.getDimensions());
            detailDTO.setCoverImage(detail.getCoverImage());
            detailDTO.setAdditionalInfo(detail.getAdditionalInfo());
            dto.setBookDetail(detailDTO);
        }

        return dto;
    }


    public BookDTO updateBook(Book book)throws IdInvalidException {
        // 🔍 Kiểm tra tồn tại
        Book existing = bookRepository.findById(book.getId())
                .orElseThrow(() -> new IdInvalidException("Book not found with id: " + book.getId()));

        // ✅ Cập nhật các trường cơ bản
        existing.setTitle(book.getTitle());
        existing.setIsbn(book.getIsbn());
        existing.setPublishYear(book.getPublishYear());
        existing.setDescription(book.getDescription());
        existing.setPrice(book.getPrice());
        existing.setDiscountPrice(book.getDiscountPrice());
        existing.setQuantity(book.getQuantity());
        existing.setStock(book.getStock());
        existing.setStatus(book.getStatus());

        // ✅ Map Authors nếu có
        if (book.getAuthors() != null) {
            List<Long> authorIds = book.getAuthors()
                    .stream().map(Author::getId)
                    .collect(Collectors.toList());
            List<Author> dbAuthors = authorRepository.findAllById(authorIds);
            existing.setAuthors(dbAuthors);
        }

        // ✅ Map Publisher nếu có
        if (book.getPublisher() != null && book.getPublisher().getId() != null) {
            publisherRepository.findById(book.getPublisher().getId())
                    .ifPresent(existing::setPublisher);
        }

        // ✅ Map Categories nếu có
        if (book.getCategories() != null) {
            List<Long> categoryIds = book.getCategories()
                    .stream().map(Category::getId)
                    .collect(Collectors.toList());
            List<Category> dbCategories = categoryRepository.findAllById(categoryIds);
            existing.setCategories(dbCategories);
        }

        // ✅ Xử lý BookDetail (cập nhật hoặc tạo mới)
        if (book.getBookDetail() != null) {
            BookDetail newDetail = book.getBookDetail();
            BookDetail existingDetail = existing.getBookDetail();

            if (existingDetail == null) {
                // Nếu sách chưa có detail → tạo mới
                newDetail.setBook(existing);
                existing.setBookDetail(newDetail);
            } else {
                // Nếu đã có → cập nhật từng trường
                existingDetail.setNumberOfPages(newDetail.getNumberOfPages());
                existingDetail.setLanguage(newDetail.getLanguage());
                existingDetail.setWeight(newDetail.getWeight());
                existingDetail.setDimensions(newDetail.getDimensions());
                existingDetail.setCoverImage(newDetail.getCoverImage());
                existingDetail.setAdditionalInfo(newDetail.getAdditionalInfo());
            }
        }

        // ✅ Lưu lại
        Book updated = bookRepository.save(existing);

        // 🔄 Convert sang DTO (tận dụng hàm convertToBookDTO để tránh lặp)
        return convertToBookDTO(updated);
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


    public ResultPaginationDTO searchBooksByAuthor(String authorName, Pageable pageable) {
        Page<Book> pageBook = bookRepository.findByAuthorName(authorName, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageBook.getTotalElements());
        meta.setPages(pageBook.getTotalPages());

        List<BookDTO> bookDTOs = pageBook.getContent()
                .stream()
                .map(this::convertToBookDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(bookDTOs);

        return result;
    }


    public ResultPaginationDTO searchBooksByPublisher(String publisherName, Pageable pageable) {
        Page<Book> pageBook = bookRepository.findByPublisherName(publisherName, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageBook.getTotalElements());
        meta.setPages(pageBook.getTotalPages());

        List<BookDTO> bookDTOs = pageBook.getContent()
                .stream()
                .map(this::convertToBookDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(bookDTOs);

        return result;
    }


    public ResultPaginationDTO searchBooksByCategory(String categoryName, Pageable pageable) {
        Page<Book> pageBook = bookRepository.findByCategoryName(categoryName, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageBook.getTotalElements());
        meta.setPages(pageBook.getTotalPages());

        List<BookDTO> bookDTOs = pageBook.getContent()
                .stream()
                .map(this::convertToBookDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(bookDTOs);

        return result;
    }

    public ResultPaginationDTO searchBooks(
            String keyword,
            Long authorId,
            Long publisherId,
            Long categoryId,
            BookStatus status,
            Pageable pageable
    ) {
        Specification<Book> spec = (root, query, cb) -> cb.conjunction();

        // 🔍 Tìm theo từ khóa (title, description)
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
            ));
        }

        // 👤 Lọc theo tác giả
        if (authorId != null) {
            spec = spec.and((root, query, cb) -> {
                Join<Object, Object> authors = root.join("authors", JoinType.LEFT);
                return cb.equal(authors.get("id"), authorId);
            });
        }

        // 🏢 Lọc theo nhà xuất bản
        if (publisherId != null) {
            spec = spec.and((root, query, cb) -> {
                Join<Object, Object> publisher = root.join("publisher", JoinType.LEFT);
                return cb.equal(publisher.get("id"), publisherId);
            });
        }

        // 🏷️ Lọc theo thể loại
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> {
                Join<Object, Object> categories = root.join("categories", JoinType.LEFT);
                return cb.equal(categories.get("id"), categoryId);
            });
        }

        // 📦 Lọc theo trạng thái (AVAILABLE, OUT_OF_STOCK, ...)
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        // ✅ Truy vấn
        Page<Book> pageBook = bookRepository.findAll(spec, pageable);

        // 📘 Map sang DTO
        List<BookDTO> bookDTOs = pageBook.getContent()
                .stream()
                .map(this::convertToBookDTO)
                .collect(Collectors.toList());

        // 🧾 Meta
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageBook.getTotalElements());
        meta.setPages(pageBook.getTotalPages());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(bookDTOs);

        return result;
    }

    //tim kiem theo ten tac gia, nxb, the loai

//    public ResultPaginationDTO searchBooks(
//            String keyword,
//            String authorName,
//            String publisherName,
//            String categoryName,
//            BookStatus status,
//            Pageable pageable
//    ) {
//        Specification<Book> spec = (root, query, cb) -> cb.conjunction();
//
//        // Tìm theo tiêu đề / mô tả
//        if (keyword != null && !keyword.isEmpty()) {
//            spec = spec.and((root, query, cb) ->
//                    cb.or(
//                            cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
//                            cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
//                    ));
//        }
//
//        // Tìm theo tên tác giả
//        if (authorName != null && !authorName.isEmpty()) {
//            spec = spec.and((root, query, cb) -> {
//                Join<Object, Object> authors = root.join("authors", JoinType.LEFT);
//                return cb.like(cb.lower(authors.get("name")), "%" + authorName.toLowerCase() + "%");
//            });
//        }
//
//        // Tìm theo nhà xuất bản
//        if (publisherName != null && !publisherName.isEmpty()) {
//            spec = spec.and((root, query, cb) -> {
//                Join<Object, Object> publisher = root.join("publisher", JoinType.LEFT);
//                return cb.like(cb.lower(publisher.get("name")), "%" + publisherName.toLowerCase() + "%");
//            });
//        }
//
//        // Tìm theo danh mục
//        if (categoryName != null && !categoryName.isEmpty()) {
//            spec = spec.and((root, query, cb) -> {
//                Join<Object, Object> categories = root.join("categories", JoinType.LEFT);
//                return cb.like(cb.lower(categories.get("name")), "%" + categoryName.toLowerCase() + "%");
//            });
//        }
//
//        // Tìm theo trạng thái
//        if (status != null) {
//            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
//        }
//
//        // Lấy kết quả
//        Page<Book> pageBook = bookRepository.findAll(spec, pageable);
//
//        // Convert
//        ResultPaginationDTO result = new ResultPaginationDTO();
//        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
//        meta.setPage(pageable.getPageNumber() + 1);
//        meta.setPageSize(pageable.getPageSize());
//        meta.setTotal(pageBook.getTotalElements());
//        meta.setPages(pageBook.getTotalPages());
//        result.setMeta(meta);
//
//        result.setResult(pageBook.getContent().stream()
//                .map(this::convertToBookDTO)
//                .collect(Collectors.toList()));
//
//        return result;
//    }


}

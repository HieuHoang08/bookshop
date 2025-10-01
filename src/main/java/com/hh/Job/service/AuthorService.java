package com.hh.Job.service;


import com.hh.Job.domain.Author;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.author.ResAuthorDTO;
import com.hh.Job.repository.AuthorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    // Create
    public Author createAuthor(Author author) {
        return authorRepository.save(author);
    }

    // Update
    public Author updateAuthor(Author author) {
        Optional<Author> authorOptional = this.authorRepository.findById(author.getId());
        if (authorOptional.isPresent()) {
            Author currentAuthor = authorOptional.get();
            currentAuthor.setName(author.getName());
            currentAuthor.setDate(author.getDate());
            currentAuthor.setDescription(author.getDescription());
            return this.authorRepository.save(currentAuthor);
        }
        return null;
    }

    // Fetch all authors (with pagination + optional filter)
    public ResultPaginationDTO fetchAllAuthors(Specification<Author> spec, Pageable pageable) {
        Page<Author> pageAuthor = authorRepository.findAll(spec, pageable);

        // Meta info
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageAuthor.getNumber() + 1);       // page hiện tại (1-based)
        meta.setPageSize(pageAuthor.getSize());         // số phần tử mỗi trang
        meta.setTotal(pageAuthor.getTotalElements());   // tổng số record
        meta.setPages(pageAuthor.getTotalPages());      // tổng số trang

        // Data
        List<ResAuthorDTO> authors = pageAuthor.getContent().stream()
                .map(author -> new ResAuthorDTO(
                        author.getId(),
                        author.getName(),
                        author.getDate(),
                        author.getDescription()
                ))
                .collect(Collectors.toList());

        // Response
        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(authors);

        return result;
    }

    // Find by ID
    public Author findAuthorById(Long id) {
        Optional<Author> authorOptional = this.authorRepository.findById(id);
        return authorOptional.orElse(null);
    }

// Delete
    public void deleteAuthor(Long id) {
        Optional<Author> authorOptional = this.authorRepository.findById(id);
        if (authorOptional.isPresent()) {
            this.authorRepository.deleteById(id);
        }
    }

}

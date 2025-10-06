package com.hh.Job.controller;


import com.hh.Job.domain.Author;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.service.AuthorService;
import com.hh.Job.util.annotation.APImessage;
import com.hh.Job.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthorController {
    private final AuthorService authorService;
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping("/authors")
    @APImessage("create a author")
    public ResponseEntity<Author> createAuthor(@RequestBody Author author) {
        Author created = authorService.createAuthor(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Update
    @PutMapping("/authors/{id}")
    @APImessage("Update an author")
    public ResponseEntity<Author> updateAuthor(@PathVariable("id") Long id, @RequestBody Author author)
    throws IdInvalidException {
        author.setId(id); // gán id từ path vào object
        Author updated = authorService.updateAuthor(author);
        if (updated == null) {
            throw  new IdInvalidException("Author voi id" + id + " not found");
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/authors")
    @APImessage("Fetch all authors with pagination and filter")
    public ResponseEntity<ResultPaginationDTO> getAllAuthors(
            @Filter Specification<Author> spec,
            Pageable pageable
    ) {
        ResultPaginationDTO response = authorService.fetchAllAuthors(spec, pageable);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/authors/{id}")
    @APImessage("Fetch author by ID")
    public ResponseEntity<Author> getAuthorById(@PathVariable("id") Long id)
    throws IdInvalidException {
        Author author = authorService.findAuthorById(id);
        if (author == null) {
            throw new IdInvalidException("Author voi id" + id + " not found");
        }
        return ResponseEntity.ok(author);
    }

    // Delete
    @DeleteMapping("/authors/{id}")
    @APImessage("Delete an author")
    public ResponseEntity<Object> deleteAuthor(@PathVariable("id") Long id)
    throws IdInvalidException {
        Author author = authorService.findAuthorById(id);
        if (author == null) {
            throw new IdInvalidException("Author voi id" + id + " not found");
        }
        this.authorService.deleteAuthor(id);
        return ResponseEntity.ok().body("null");
    }
}

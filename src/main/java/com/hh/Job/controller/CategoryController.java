package com.hh.Job.controller;


import com.hh.Job.domain.Category;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.repository.CategoryRepository;
import com.hh.Job.service.CategoryService;
import com.hh.Job.service.UserService;
import com.hh.Job.util.annotation.APImessage;
import com.hh.Job.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryService categoryService, CategoryRepository categoryRepository) {

        this.categoryService = categoryService;
        this.categoryRepository = categoryRepository;
    }

    @PostMapping("/categories")
    @APImessage("create a caregories")
    public ResponseEntity<Category> createCategory(@RequestBody Category category)
    throws IdInvalidException {
        boolean isNameExit = this.categoryRepository.existsByName(category.getName());
        if (isNameExit) {
            throw new IdInvalidException("Category" + category.getName() + " da ton tai");
        }
        Category cate = this.categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(cate);
    }

    @GetMapping("/categories/{id}")
    @APImessage("fetch an category byID")
    public ResponseEntity<Category> getCategoryById(@PathVariable("id") Long id)
        throws IdInvalidException {
        Category cate = this.categoryService.findCategoryById(id);
        if (cate == null) {
            throw new IdInvalidException("Category" + id + " not found");
        }
        return ResponseEntity.ok(cate);
    }

    @PutMapping("/categories/{id}")
    @APImessage("update an category")
    public ResponseEntity<Category> updateCategory(
            @PathVariable("id") Long id,
            @RequestBody Category category
    ) throws IdInvalidException {
        category.setId(id);
        Category updated = this.categoryService.updateCategory(category);
        if (updated == null) {
            throw new IdInvalidException("Category " + id + " not found");
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/categories/{id}")
    @APImessage("Delete an category")
    public ResponseEntity<Object> deleteCategory(@PathVariable("id") Long id) throws IdInvalidException {
        Category category = this.categoryService.findCategoryById(id);
        if (category == null) {
            throw new IdInvalidException("Category " + id + " not found");
        }
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().body("delete publisher successfully");
    }

    @GetMapping("/categories")
    @APImessage("fetch all category")
    public ResponseEntity<ResultPaginationDTO> getAllCategories(
            @Filter Specification<Category> spec,
            Pageable pageable
    ) {
        ResultPaginationDTO result = categoryService.fetchAllCategories(spec, pageable);
        return  ResponseEntity.ok(result);
    }
}

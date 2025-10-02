package com.hh.Job.service;


import com.hh.Job.domain.Category;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.category.ResCategoryDTO;
import com.hh.Job.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category updateCategory(Category category) {
        Optional<Category> optional = categoryRepository.findById(category.getId());
        if (optional.isPresent()) {
            Category current = optional.get();
            current.setName(category.getName());
            current.setDescription(category.getDescription());
            return categoryRepository.save(current);
        }
        return null;
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    public Category findCategoryById(Long id) {
        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public ResultPaginationDTO fetchAllCategories(Specification<Category> spec, Pageable pageable) {
        Page<Category> pageCategory = this.categoryRepository.findAll(spec, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1); // số trang hiện tại
        meta.setPageSize(pageable.getPageSize());  // số phần tử mỗi trang
        meta.setTotal(pageCategory.getTotalElements()); // tổng số bản ghi
        meta.setPages(pageCategory.getTotalPages());    // tổng số trang

        List<ResCategoryDTO> listCategory = pageCategory.getContent().stream()
                .map(category -> new ResCategoryDTO(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                )).collect(Collectors.toList());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(listCategory);

        return result;
    }
}

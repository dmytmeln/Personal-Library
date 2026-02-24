package org.example.library.category.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.category.dto.CategoryDto;
import org.example.library.category.dto.CategorySearchParams;
import org.example.library.category.dto.CategoryWithBooksCount;
import org.example.library.category.service.CategoryService;
import org.example.library.pagination.PaginationParams;
import org.example.library.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    public Page<CategoryWithBooksCount> getAll(
            PaginationParams paginationParams,
            CategorySearchParams searchParams
    ) {
        return service.search(paginationParams, searchParams);
    }

    @GetMapping("/me")
    public Page<CategoryWithBooksCount> getAllForUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            PaginationParams paginationParams,
            CategorySearchParams searchParams
    ) {
        return service.searchForUser(userDetails.getId(), paginationParams, searchParams);
    }

    @GetMapping("/{categoryId}")
    public CategoryDto getById(@PathVariable Integer categoryId) {
        return service.getById(categoryId);
    }

}

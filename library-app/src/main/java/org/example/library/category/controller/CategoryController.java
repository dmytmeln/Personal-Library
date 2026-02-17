package org.example.library.category.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.category.dto.CategoryDto;
import org.example.library.category.service.CategoryService;
import org.example.library.pagination.PaginationParams;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    public Page<CategoryDto> getAll(
            @RequestParam(required = false) String name,
            PaginationParams paginationParams
    ) {
        return service.search(name, paginationParams);
    }

}

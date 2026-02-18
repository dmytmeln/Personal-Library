package org.example.library.category.service;

import lombok.RequiredArgsConstructor;
import org.example.library.category.domain.Category;
import org.example.library.category.dto.CategoryDto;
import org.example.library.category.dto.CategorySearchParams;
import org.example.library.category.dto.CategoryWithBooksCount;
import org.example.library.category.mapper.CategoryMapper;
import org.example.library.category.repository.CategoryRepository;
import org.example.library.exception.NotFoundException;
import org.example.library.pagination.PageRequestBuilder;
import org.example.library.pagination.PaginationParams;
import org.example.library.pagination.SortableFields;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;
    private final PageRequestBuilder pageRequestBuilder;

    public Page<CategoryWithBooksCount> search(PaginationParams paginationParams, CategorySearchParams searchParams) {
        var pageable = pageRequestBuilder.buildPageRequest(paginationParams, SortableFields.CATEGORY_FIELDS);
        return repository.searchWithBooksCount(
                searchParams.getName(),
                searchParams.getBooksCountMin(),
                searchParams.getBooksCountMax(),
                pageable
        );
    }

    public Category getExistingById(Integer categoryId) {
        return repository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    public CategoryDto getById(Integer categoryId) {
        return mapper.toDto(getExistingById(categoryId));
    }

}

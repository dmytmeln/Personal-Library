package org.example.library.category.service;

import lombok.RequiredArgsConstructor;
import org.example.library.category.domain.Category;
import org.example.library.category.dto.CategoryDto;
import org.example.library.category.mapper.CategoryMapper;
import org.example.library.category.repository.CategoryRepository;
import org.example.library.exception.NotFoundException;
import org.example.library.pagination.PaginationParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public Page<CategoryDto> search(String name, PaginationParams paginationParams) {
        var pageable = PageRequest.of(paginationParams.getPage(), paginationParams.getSize());
        if (name == null || name.isBlank()) {
            return repository.findAll(pageable).map(mapper::toDto);
        }
        return repository.findByNameContainingIgnoreCase(name, pageable).map(mapper::toDto);
    }

    public Category getExistingById(Integer categoryId) {
        return repository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    public CategoryDto getById(Integer categoryId) {
        return mapper.toDto(getExistingById(categoryId));
    }

}

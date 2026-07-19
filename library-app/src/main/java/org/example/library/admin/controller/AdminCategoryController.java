package org.example.library.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.admin.dto.AdminCategoryDto;
import org.example.library.admin.service.AdminCategoryService;
import org.example.library.library_book.dto.BulkRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @GetMapping("/{id}")
    public AdminCategoryDto getCategory(@PathVariable Integer id) {
        return adminCategoryService.getCategory(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createCategory(@RequestBody AdminCategoryDto dto) {
        adminCategoryService.createCategory(dto);
    }

    @PutMapping("/{id}")
    public void updateCategory(@PathVariable Integer id, @RequestBody AdminCategoryDto dto) {
        adminCategoryService.updateCategory(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Integer id) {
        adminCategoryService.deleteCategory(id);
    }

    @PostMapping("/bulk-delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategories(@RequestBody BulkRequest request) {
        adminCategoryService.deleteCategories(request.getIds());
    }

}

package org.example.library.admin.service;

import org.example.library.admin.dto.AdminCategoryDto;
import org.example.library.category.repository.CategoryRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminCategoryServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AdminCategoryService categoryService;

    @Test
    void shouldReturnCategoryWhenGetById() {
        var category = saveCategory(c -> c.name("Category Name"));

        var result = categoryService.getCategory(category.getId());

        assertThat(result.getId()).isEqualTo(category.getId());
        assertThat(result.getTranslations().get("en").getName()).isEqualTo("Category Name");
    }

    @Test
    void shouldThrowNotFoundWhenGetCategoryByIdWithoutExistingCategory() {
        assertThatThrownBy(() -> categoryService.getCategory(-1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.category.not_found");
    }

    @Test
    void shouldCreateCategory() {
        var dto = AdminCategoryDto.builder()
                .translations(Map.of("en", AdminCategoryDto.AdminCategoryTranslationDto.builder()
                        .name("New Category")
                        .description("Desc")
                        .build()))
                .build();

        categoryService.createCategory(dto);

        var categories = categoryRepository.findAll();
        assertThat(categories).hasSize(1);
        var createdCategory = testDbClient.findCategoryById(categories.get(0).getId());
        assertThat(createdCategory).isNotNull();
        assertThat(createdCategory.getTranslations().get("en").getName()).isEqualTo("New Category");
    }

    @Test
    void shouldUpdateCategory() {
        var category = saveCategory(c -> c.name("Old Name"));
        var dto = AdminCategoryDto.builder()
                .translations(Map.of("en", AdminCategoryDto.AdminCategoryTranslationDto.builder()
                        .name("New Name")
                        .build()))
                .build();

        categoryService.updateCategory(category.getId(), dto);

        var updatedCategory = testDbClient.findCategoryById(category.getId());
        assertThat(updatedCategory).isNotNull();
        assertThat(updatedCategory.getTranslations().get("en").getName()).isEqualTo("New Name");
    }

    @Test
    void shouldDeleteCategory() {
        var category = saveCategory(c -> c.name("Category"));

        categoryService.deleteCategory(category.getId());

        assertThat(testDbClient.findCategoryById(category.getId())).isNull();
    }

    @Test
    void shouldThrowBadRequestWhenDeleteCategoryWithBooks() {
        var category = saveCategory(c -> c.name("Category"));
        saveBook(b -> b.title("Book").bookLanguage("English").category(category));

        assertThatThrownBy(() -> categoryService.deleteCategory(category.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.category.has_books");
    }

    @Test
    void shouldDeleteCategoriesBulk() {
        var c1 = saveCategory(c -> c.name("C1"));
        var c2 = saveCategory(c -> c.name("C2"));

        categoryService.deleteCategories(List.of(c1.getId(), c2.getId()));

        assertThat(testDbClient.countCategories()).isZero();
    }

    @Test
    void shouldThrowBadRequestWhenDeleteCategoriesBulkWithBooks() {
        var c1 = saveCategory(c -> c.name("C1"));
        var c2 = saveCategory(c -> c.name("C2"));
        saveBook(b -> b.title("Book").bookLanguage("English").category(c1));

        assertThatThrownBy(() -> categoryService.deleteCategories(List.of(c1.getId(), c2.getId())))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.category.has_books");
    }

}

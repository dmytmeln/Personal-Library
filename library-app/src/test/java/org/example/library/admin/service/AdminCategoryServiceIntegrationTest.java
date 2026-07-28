package org.example.library.admin.service;

import org.example.library.admin.dto.AdminCategoryDto;
import org.example.library.author.domain.Author;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.category.repository.CategoryRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.book.domain.BookStatus.PRELIMINARY;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class AdminCategoryServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AdminCategoryService categoryService;

    @BeforeEach
    void cleanDbBefore() {
        testDbClient.cleanDatabase();
    }

    @AfterEach
    void tearDownEach() {
        testDbClient.cleanDatabase();
    }

    @Test
    void shouldReturnCategoryWhenGetById() {
        var category = saveCategory("Category Name");

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
        var category = saveCategory("Old Name");
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
        var category = saveCategory("Category");

        categoryService.deleteCategory(category.getId());

        assertThat(testDbClient.findCategoryById(category.getId())).isNull();
    }

    @Test
    void shouldThrowBadRequestWhenDeleteCategoryWithBooks() {
        var category = saveCategory("Category");
        saveBook(category, Set.of());

        assertThatThrownBy(() -> categoryService.deleteCategory(category.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.category.has_books");
    }

    @Test
    void shouldDeleteCategoriesBulk() {
        var c1 = saveCategory("C1");
        var c2 = saveCategory("C2");

        categoryService.deleteCategories(List.of(c1.getId(), c2.getId()));

        assertThat(testDbClient.countCategories()).isZero();
    }

    @Test
    void shouldThrowBadRequestWhenDeleteCategoriesBulkWithBooks() {
        var c1 = saveCategory("C1");
        var c2 = saveCategory("C2");
        saveBook(c1, Set.of());

        assertThatThrownBy(() -> categoryService.deleteCategories(List.of(c1.getId(), c2.getId())))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.category.has_books");
    }

    private Category saveCategory(String name) {
        var category = Category.builder()
                .popularityCount(0)
                .build();

        var translation = CategoryTranslation.builder()
                .languageCode("en")
                .name(name)
                .description("Desc " + name)
                .category(category)
                .build();
        category.setTranslations(new HashMap<>(Map.of("en", translation)));

        testDbClient.saveCategory(category);
        return category;
    }

    private void saveBook(Category category, Set<Author> authors) {
        var book = Book.builder()
                .category(category)
                .authors(authors)
                .publishYear((short) 2000)
                .pages((short) 200)
                .status(PRELIMINARY)
                .popularityCount(0)
                .build();

        var translation = BookTranslation.builder()
                .languageCode("en")
                .title("Book")
                .bookLanguage("English")
                .description("Desc Book")
                .book(book)
                .build();
        book.setTranslations(new HashMap<>(Map.of("en", translation)));

        testDbClient.saveBook(book);
        if (authors != null) {
            for (Author author : authors) {
                testDbClient.linkBookToAuthor(book.getId(), author.getId());
            }
        }
    }

}

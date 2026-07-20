package org.example.library.admin.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.library.admin.dto.AdminCategoryDto;
import org.example.library.author.domain.Author;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookStatus;
import org.example.library.book.domain.BookTranslation;
import org.example.library.book.repository.BookRepository;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.category.repository.CategoryRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.PostgresTestContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@ContextConfiguration(initializers = PostgresTestContainer.class)
class AdminCategoryServiceIntegrationTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AdminCategoryService categoryService;

    @Test
    void shouldReturnCategoryWhenGetById() {
        var category = saveCategory("Category Name");
        em.flush();
        em.clear();

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
        em.flush();
        em.clear();

        var categories = categoryRepository.findAll();
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getTranslations().get("en").getName()).isEqualTo("New Category");
    }

    @Test
    void shouldUpdateCategory() {
        var category = saveCategory("Old Name");
        var dto = AdminCategoryDto.builder()
                .translations(Map.of("en", AdminCategoryDto.AdminCategoryTranslationDto.builder()
                        .name("New Name")
                        .build()))
                .build();
        em.flush();
        em.clear();

        categoryService.updateCategory(category.getId(), dto);
        em.flush();
        em.clear();

        var updatedCategory = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new AssertionError("Category not found after update"));
        assertThat(updatedCategory.getTranslations().get("en").getName()).isEqualTo("New Name");
    }

    @Test
    void shouldDeleteCategory() {
        var category = saveCategory("Category");
        em.flush();
        em.clear();

        categoryService.deleteCategory(category.getId());
        em.flush();
        em.clear();

        assertThat(categoryRepository.existsById(category.getId())).isFalse();
    }

    @Test
    void shouldThrowBadRequestWhenDeleteCategoryWithBooks() {
        var category = saveCategory("Category");
        saveBook(category, Set.of());
        em.flush();
        em.clear();

        assertThatThrownBy(() -> categoryService.deleteCategory(category.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.category.has_books");
    }

    @Test
    void shouldDeleteCategoriesBulk() {
        var c1 = saveCategory("C1");
        var c2 = saveCategory("C2");
        em.flush();
        em.clear();

        categoryService.deleteCategories(List.of(c1.getId(), c2.getId()));
        em.flush();
        em.clear();

        assertThat(categoryRepository.findAll()).isEmpty();
    }

    @Test
    void shouldThrowBadRequestWhenDeleteCategoriesBulkWithBooks() {
        var c1 = saveCategory("C1");
        var c2 = saveCategory("C2");
        saveBook(c1, Set.of());
        em.flush();
        em.clear();

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

        return categoryRepository.save(category);
    }

    private void saveBook(Category category, Set<Author> authors) {
        var book = Book.builder()
                .category(category)
                .authors(authors)
                .publishYear((short) 2000)
                .pages((short) 200)
                .status(BookStatus.PRELIMINARY)
                .popularityCount(0)
                .build();

        var translation = BookTranslation.builder()
                .languageCode("en")
                .title("Book")
                .bookLanguage("English")
                .description("Desc " + "Book")
                .book(book)
                .build();
        book.setTranslations(new HashMap<>(Map.of("en", translation)));

        bookRepository.save(book);
    }

}

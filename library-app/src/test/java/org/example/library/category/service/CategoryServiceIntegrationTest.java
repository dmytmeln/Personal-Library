package org.example.library.category.service;

import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.category.dto.CategorySearchParams;
import org.example.library.common.exception.NotFoundException;
import org.example.library.common.pagination.PaginationParams;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.book.domain.BookStatus.PRELIMINARY;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class CategoryServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private CategoryService service;

    @BeforeAll
    static void setUp() {
        LocaleContextHolder.setLocale(ENGLISH);
    }

    @BeforeEach
    void cleanDbBefore() {
        testDbClient.cleanDatabase();
    }

    @AfterEach
    void tearDownEach() {
        testDbClient.cleanDatabase();
        LocaleContextHolder.resetLocaleContext();
        LocaleContextHolder.setLocale(ENGLISH);
    }

    @AfterAll
    static void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void shouldReturnCategoryWhenGetById() {
        var expected = saveCategory("Test Category");

        var existingCategory = service.getById(expected.getId());

        assertThat(existingCategory.name()).isEqualTo(expected.getTranslations().get("en").getName());
        assertThat(existingCategory.description()).isEqualTo(expected.getTranslations().get("en").getDescription());
    }

    @Test
    void shouldThrowNotFoundWhenGetByIdWithoutExistingCategory() {
        assertThatThrownBy(() -> service.getById(-99999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.category.not_found");
    }

    @Test
    void shouldSearchCategoriesWithBooksCount() {
        var category = saveCategory("Fiction");
        saveBook(category);
        saveBook(category);
        var otherCategory = saveCategory("Science");
        saveBook(otherCategory);
        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setSort(List.of("name;asc"));
        var searchParams = new CategorySearchParams();
        searchParams.setName("Fiction");

        var result = service.search(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Fiction");
        assertThat(result.getContent().get(0).getBooksCount()).isEqualTo(2);
    }

    @Test
    void shouldFindCategoryWithTypo() {
        saveCategory("History");
        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new CategorySearchParams();
        searchParams.setName("Histoy");

        var result = service.search(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("History");
    }

    @Test
    void shouldSearchCategoriesByBooksCountRange() {
        var cat1 = saveCategory("Cat 1");
        saveBook(cat1);
        var cat2 = saveCategory("Cat 2");
        saveBook(cat2);
        saveBook(cat2);
        var cat3 = saveCategory("Cat 3");
        saveBook(cat3);
        saveBook(cat3);
        saveBook(cat3);
        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setSort(List.of("name;asc"));
        var searchParams = new CategorySearchParams();
        searchParams.setBooksCountMin(2);
        searchParams.setBooksCountMax(2);

        var result = service.search(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Cat 2");
        assertThat(result.getContent().get(0).getBooksCount()).isEqualTo(2);
    }

    @Test
    void shouldSearchCategoriesForUser() {
        var user = User.builder().email("test@example.com").fullName("Test User").password("pass").role(USER).build();
        testDbClient.saveUser(user);
        var otherUser = User.builder().email("other@example.com").fullName("Other User").password("pass").role(USER).build();
        testDbClient.saveUser(otherUser);
        var category = saveCategory("User Category");
        var book1 = saveBook(category);
        var book2 = saveBook(category);
        var book3 = saveBook(category);
        testDbClient.saveLibraryBook(LibraryBook.builder().user(user).book(book1).title("Title 1").build());
        testDbClient.saveLibraryBook(LibraryBook.builder().user(user).book(book2).title("Title 2").build());
        testDbClient.saveLibraryBook(LibraryBook.builder().user(otherUser).book(book3).title("Title 3").build());
        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setSort(List.of("name;asc"));
        var searchParams = new CategorySearchParams();

        var result = service.searchForUser(user.getId(), pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("User Category");
        assertThat(result.getContent().get(0).getBooksCount()).isEqualTo(2);
    }

    private Category saveCategory(String name) {
        var category = Category.builder()
                .popularityCount(0)
                .build();

        var translation = CategoryTranslation.builder()
                .languageCode("en")
                .name(name)
                .description("Description of " + name)
                .category(category)
                .build();
        category.setTranslations(new HashMap<>(Map.of("en", translation)));

        testDbClient.saveCategory(category);
        return category;
    }

    private Book saveBook(Category category) {
        var book = Book.builder()
                .category(category)
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
        return book;
    }

}

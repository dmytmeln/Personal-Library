package org.example.library.book.service;

import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.book.dto.BookSearchParams;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.common.pagination.PaginationParams;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.book.domain.BookStatus.PRELIMINARY;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class BookServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private BookService service;

    private Category defaultCategory;

    @BeforeAll
    static void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @BeforeEach
    void cleanDbBefore() {
        testDbClient.cleanDatabase();
        defaultCategory = saveCategory("Default Category");
    }

    @AfterEach
    void tearDownEach() {
        testDbClient.cleanDatabase();
    }

    @Test
    void shouldGetAllBooks() {
        saveBook("Book 1", "English");
        saveBook("Book 2", "English");

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldFilterBooksByTitle() {
        saveBook("Spring in Action", "English");
        saveBook("Java Persistence with Hibernate", "English");

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();
        searchParams.setTitle("Spring");

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Spring in Action");
    }

    @Test
    void shouldFindBookWithTypo() {
        saveBook("The Great Gatsby", "English");

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();
        searchParams.setTitle("The Great Gatsbyy");

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("The Great Gatsby");
    }

    @Test
    void shouldFilterBooksByCategoryId() {
        var category = saveCategory("Fiction");
        var otherCategory = saveCategory("Sci-Fi");
        saveBook("Fictional Story", category);
        saveBook("Sci-Fi Adventure", otherCategory);

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();
        searchParams.setCategoryId(category.getId());

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategoryId()).isEqualTo(category.getId());
    }

    @Test
    void shouldFilterBooksByAuthorId() {
        var author = saveAuthor("Author 1");
        var otherAuthor = saveAuthor("Author 2");
        saveBook("Book by A1", author);
        saveBook("Book by A2", otherAuthor);

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();
        searchParams.setAuthorId(author.getId());

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAuthors().containsKey(author.getId())).isTrue();
    }

    @Test
    void shouldFilterBooksByPublishYearRange() {
        saveBook("Old Book", (short) 1990);
        saveBook("New Book", (short) 2020);
        saveBook("Mid Book", (short) 2005);

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();
        searchParams.setPublishYearMin((short) 2000);
        searchParams.setPublishYearMax((short) 2010);

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Mid Book");
    }

    @Test
    void shouldFilterBooksByLanguages() {
        saveBook("English Book", "English");
        saveBook("French Book", "French");

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();
        searchParams.setLanguages(List.of("French"));

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLanguage()).isEqualTo("French");
    }

    @Test
    void shouldReturnAllLanguages() {
        saveBook("B1", "English");
        saveBook("B2", "English");
        saveBook("B3", "French");

        var languages = service.getAllLanguages();

        assertThat(languages).hasSize(2);
        assertThat(languages).extracting("language").containsExactlyInAnyOrder("English", "French");
        assertThat(languages).filteredOn(l -> l.getLanguage().equals("English"))
                .extracting("count").containsExactly(2L);
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
        category.setTranslations(Map.of("en", translation));

        testDbClient.saveCategory(category);
        return category;
    }

    private Author saveAuthor(String fullName) {
        var author = Author.builder()
                .birthYear((short) 1900)
                .popularityCount(0)
                .build();

        var translation = AuthorTranslation.builder()
                .languageCode("en")
                .fullName(fullName)
                .country("Country")
                .author(author)
                .build();
        author.setTranslations(Map.of("en", translation));

        testDbClient.saveAuthor(author);
        return author;
    }

    private void saveBook(String title, String bookLanguage) {
        saveBook(title, bookLanguage, defaultCategory, (short) 2000, null);
    }

    private void saveBook(String title, Category category) {
        saveBook(title, "English", category, (short) 2000, null);
    }

    private void saveBook(String title, Author author) {
        saveBook(title, "English", defaultCategory, (short) 2000, author);
    }

    private void saveBook(String title, short publishYear) {
        saveBook(title, "English", defaultCategory, publishYear, null);
    }

    private void saveBook(String title, String bookLanguage, Category category, short publishYear, Author author) {
        var book = Book.builder()
                .category(category)
                .publishYear(publishYear)
                .popularityCount(0)
                .status(PRELIMINARY)
                .authors(author != null ? Set.of(author) : Set.of())
                .build();

        var translation = BookTranslation.builder()
                .languageCode("en")
                .title(title)
                .bookLanguage(bookLanguage)
                .description("Description of " + title)
                .book(book)
                .build();
        book.setTranslations(Map.of("en", translation));

        testDbClient.saveBook(book);
        if (author != null) {
            testDbClient.linkBookToAuthor(book.getId(), author.getId());
        }
    }

}

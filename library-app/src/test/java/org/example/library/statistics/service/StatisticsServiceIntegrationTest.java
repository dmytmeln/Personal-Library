package org.example.library.statistics.service;

import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
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

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.example.library.book.domain.BookStatus.PRELIMINARY;
import static org.example.library.library_book.domain.LibraryBookStatus.READ;
import static org.example.library.library_book.domain.LibraryBookStatus.READING;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class StatisticsServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private StatisticsService service;

    @BeforeAll
    static void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @BeforeEach
    void cleanDbBefore() {
        testDbClient.cleanDatabase();
    }

    @AfterEach
    void tearDownEach() {
        testDbClient.cleanDatabase();
        LocaleContextHolder.resetLocaleContext();
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterAll
    static void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void shouldReturnDashboardStats() {
        var user = User.builder()
                .email("stats_test@example.com")
                .fullName("Stats Test User")
                .password("pass")
                .role(USER)
                .build();
        testDbClient.saveUser(user);
        var category = saveCategory();
        var author = saveAuthor();
        var book = saveBook(category, author);
        var lb1 = LibraryBook.builder()
                .user(user)
                .book(book)
                .status(READ)
                .finishedAt(LocalDate.of(2023, 5, 10))
                .rating((byte) 5)
                .pages((short) 300)
                .language("English")
                .build();
        testDbClient.saveLibraryBook(lb1);
        var lb2 = LibraryBook.builder()
                .user(user)
                .book(book)
                .status(READING)
                .build();
        testDbClient.saveLibraryBook(lb2);
        var lb3 = LibraryBook.builder()
                .user(user)
                .book(book)
                .status(READ)
                .finishedAt(LocalDate.of(2023, 11, 15))
                .rating((byte) 4)
                .build();
        testDbClient.saveLibraryBook(lb3);

        var stats = service.getDashboardStats(user.getId(), 2023);

        assertThat(stats).isNotNull();
        var summary = stats.getSummary();
        assertThat(summary.getTotalLibraryBooks()).isEqualTo(3L);
        assertThat(summary.getBooksReadCount()).isEqualTo(2L);
        assertThat(summary.getPagesReadCount()).isEqualTo(600L);
        assertThat(summary.getAverageRating()).isEqualTo(4.5);
        assertThat(summary.getCurrentlyReadingCount()).isEqualTo(1L);
        assertThat(summary.getTotalRatedBooks()).isEqualTo(2L);
        assertThat(stats.getCategoryDistribution()).hasSize(1);
        assertThat(stats.getCategoryDistribution().get(0).getCategoryName()).isEqualTo("Fiction");
        assertThat(stats.getCategoryDistribution().get(0).getCount()).isEqualTo(3L);
        assertThat(stats.getStatusDistribution()).hasSize(2);
        assertThat(stats.getStatusDistribution())
                .filteredOn(s -> s.getStatus() == READ)
                .extracting("count")
                .containsExactly(2L);
        assertThat(stats.getStatusDistribution())
                .filteredOn(s -> s.getStatus() == READING)
                .extracting("count")
                .containsExactly(1L);
        assertThat(stats.getLanguageDistribution()).hasSize(1);
        assertThat(stats.getLanguageDistribution())
                .extracting("language", "count")
                .containsExactly(tuple("English", 3L));
        assertThat(stats.getAuthorCountryDistribution()).hasSize(1);
        assertThat(stats.getAuthorCountryDistribution())
                .extracting("country", "count")
                .containsExactly(tuple("UK", 3L));
        assertThat(stats.getMonthlyReadingActivity()).hasSize(2);
        assertThat(stats.getMonthlyReadingActivity())
                .filteredOn(m -> m.getMonth() == 5)
                .extracting("count")
                .containsExactly(1L);
        assertThat(stats.getMonthlyReadingActivity())
                .filteredOn(m -> m.getMonth() == 11)
                .extracting("count")
                .containsExactly(1L);
        assertThat(stats.getTopAuthors()).hasSize(1);
        assertThat(stats.getTopAuthors())
                .extracting("authorName", "count")
                .containsExactly(tuple("Author 1", 3L));
    }

    private Category saveCategory() {
        var category = Category.builder()
                .popularityCount(0)
                .build();

        var translation = CategoryTranslation.builder()
                .category(category)
                .languageCode("en")
                .name("Fiction")
                .description("Description of Fiction")
                .build();
        category.setTranslations(Map.of("en", translation));

        testDbClient.saveCategory(category);
        return category;
    }

    private Author saveAuthor() {
        var author = Author.builder()
                .birthYear((short) 1970)
                .popularityCount(0)
                .build();

        var translation = AuthorTranslation.builder()
                .author(author)
                .languageCode("en")
                .fullName("Author 1")
                .country("UK")
                .build();
        author.setTranslations(Map.of("en", translation));

        testDbClient.saveAuthor(author);
        return author;
    }

    private Book saveBook(Category category, Author author) {
        var book = Book.builder()
                .category(category)
                .pages((short) 300)
                .status(PRELIMINARY)
                .popularityCount(0)
                .authors(Set.of(author))
                .build();

        var translation = BookTranslation.builder()
                .book(book)
                .languageCode("en")
                .title("Title")
                .bookLanguage("English")
                .description("Desc")
                .build();
        book.setTranslations(Map.of("en", translation));

        testDbClient.saveBook(book);
        testDbClient.linkBookToAuthor(book.getId(), author.getId());

        return book;
    }

}

package org.example.library.statistics.repository;

import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.config.AbstractRepositoryTest;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.domain.LibraryBookStatus;
import org.example.library.statistics.dto.AuthorCountryDistributionDto;
import org.example.library.statistics.dto.CategoryDistributionDto;
import org.example.library.statistics.dto.DashboardSummaryDto;
import org.example.library.statistics.dto.LanguageDistributionDto;
import org.example.library.statistics.dto.MonthlyReadingActivityDto;
import org.example.library.statistics.dto.StatusDistributionDto;
import org.example.library.statistics.dto.TopAuthorDto;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.example.library.user.domain.Role.USER;

class StatisticsRepositoryTest extends AbstractRepositoryTest<StatisticsRepository> {

    @Test
    void getSummary_ShouldReturnSummaryDto() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book1 = createBook(category, 100);
        testDbClient.saveBook(book1);
        Book book2 = createBook(category, 200);
        testDbClient.saveBook(book2);
        LibraryBook lb1 = LibraryBook.builder()
                .book(book1)
                .user(user)
                .status(LibraryBookStatus.READ)
                .finishedAt(LocalDate.of(2026, 5, 10))
                .rating((byte) 4)
                .addedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .pages((short) 120)
                .build();
        testDbClient.saveLibraryBook(lb1);
        LibraryBook lb2 = LibraryBook.builder()
                .book(book2)
                .user(user)
                .status(LibraryBookStatus.READING)
                .addedAt(LocalDateTime.of(2026, 2, 1, 10, 0))
                .pages(null)
                .build();
        testDbClient.saveLibraryBook(lb2);
        LibraryBook lb3 = LibraryBook.builder()
                .book(book1)
                .user(user)
                .status(LibraryBookStatus.READ)
                .finishedAt(LocalDate.of(2026, 6, 10))
                .rating((byte) 5)
                .addedAt(LocalDateTime.of(2026, 3, 1, 10, 0))
                .pages(null)
                .build();
        testDbClient.saveLibraryBook(lb3);

        DashboardSummaryDto actual = repository.getSummary(user.getId(), 2026);

        assertThat(actual.getTotalLibraryBooks()).isEqualTo(3L);
        assertThat(actual.getBooksReadCount()).isEqualTo(2L);
        assertThat(actual.getPagesReadCount()).isEqualTo(220L);
        assertThat(actual.getAverageRating()).isEqualTo(4.5);
        assertThat(actual.getCurrentlyReadingCount()).isEqualTo(1L);
        assertThat(actual.getBooksAddedThisYear()).isEqualTo(3L);
        assertThat(actual.getTotalRatedBooks()).isEqualTo(2L);
    }

    @Test
    void getCategoryDistribution_ShouldReturnCategoryDistribution() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(category, 100);
        testDbClient.saveBook(book);
        LibraryBook lb = LibraryBook.builder()
                .book(book)
                .user(user)
                .status(LibraryBookStatus.READING)
                .addedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        testDbClient.saveLibraryBook(lb);

        List<CategoryDistributionDto> actual = repository.getCategoryDistribution(user.getId(), "en");

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getCategoryId()).isEqualTo(category.getId());
        assertThat(actual.get(0).getCategoryName()).isEqualTo("Sci-Fi");
        assertThat(actual.get(0).getCount()).isEqualTo(1L);
    }

    @Test
    void getStatusDistribution_ShouldReturnStatusDistribution() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(category, 100);
        testDbClient.saveBook(book);
        LibraryBook lb1 = LibraryBook.builder()
                .book(book)
                .user(user)
                .status(LibraryBookStatus.READ)
                .addedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        testDbClient.saveLibraryBook(lb1);
        LibraryBook lb2 = LibraryBook.builder()
                .book(book)
                .user(user)
                .status(LibraryBookStatus.READING)
                .addedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        testDbClient.saveLibraryBook(lb2);

        List<StatusDistributionDto> actual = repository.getStatusDistribution(user.getId());

        assertThat(actual).hasSize(2);
        assertThat(actual).extracting(StatusDistributionDto::getStatus)
                .containsExactlyInAnyOrder(LibraryBookStatus.READ, LibraryBookStatus.READING);
    }

    @Test
    void getLanguageDistribution_ShouldReturnLanguageDistribution() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book1 = createBook(category, 100);
        testDbClient.saveBook(book1);
        Book book2 = createBook(category, 200);
        testDbClient.saveBook(book2);
        LibraryBook lb1 = LibraryBook.builder()
                .book(book1)
                .user(user)
                .status(LibraryBookStatus.READING)
                .language("Spanish")
                .addedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        testDbClient.saveLibraryBook(lb1);
        LibraryBook lb2 = LibraryBook.builder()
                .book(book2)
                .user(user)
                .status(LibraryBookStatus.READING)
                .language(null)
                .addedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        testDbClient.saveLibraryBook(lb2);

        List<LanguageDistributionDto> actual = repository.getLanguageDistribution(user.getId(), "en");

        assertThat(actual).hasSize(2);
        assertThat(actual).extracting(LanguageDistributionDto::getLanguage)
                .containsExactlyInAnyOrder("Spanish", "English");
    }

    @Test
    void getAuthorCountryDistribution_ShouldReturnAuthorCountryDistribution() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Author author = createAuthor();
        testDbClient.saveAuthor(author);
        Book book = createBook(category, 100);
        testDbClient.saveBook(book);
        testDbClient.linkBookToAuthor(book.getId(), author.getId());
        LibraryBook lb = LibraryBook.builder()
                .book(book)
                .user(user)
                .status(LibraryBookStatus.READING)
                .addedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        testDbClient.saveLibraryBook(lb);

        List<AuthorCountryDistributionDto> actual = repository.getAuthorCountryDistribution(user.getId(), "en");

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getCountry()).isEqualTo("United Kingdom");
        assertThat(actual.get(0).getCount()).isEqualTo(1L);
    }

    @Test
    void getMonthlyReadingActivity_ShouldReturnMonthlyReadingActivity() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(category, 100);
        testDbClient.saveBook(book);
        LibraryBook lb = LibraryBook.builder()
                .book(book)
                .user(user)
                .status(LibraryBookStatus.READ)
                .finishedAt(LocalDate.of(2026, 5, 10))
                .addedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        testDbClient.saveLibraryBook(lb);

        List<MonthlyReadingActivityDto> actual = repository.getMonthlyReadingActivity(user.getId(), 2026);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getMonth()).isEqualTo(5);
        assertThat(actual.get(0).getCount()).isEqualTo(1L);
    }

    @Test
    void getTopAuthors_ShouldReturnTopAuthors() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Author author = createAuthor();
        testDbClient.saveAuthor(author);
        Book book = createBook(category, 100);
        testDbClient.saveBook(book);
        testDbClient.linkBookToAuthor(book.getId(), author.getId());
        LibraryBook lb = LibraryBook.builder()
                .book(book)
                .user(user)
                .status(LibraryBookStatus.READING)
                .addedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        testDbClient.saveLibraryBook(lb);

        List<TopAuthorDto> actual = repository.getTopAuthors(user.getId(), "en");

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getAuthorId()).isEqualTo(author.getId());
        assertThat(actual.get(0).getAuthorName()).isEqualTo("Arthur C. Clarke");
        assertThat(actual.get(0).getCount()).isEqualTo(1L);
    }

    private User createUser(String email) {
        return User.builder()
                .email(email)
                .fullName("Test User")
                .password("password")
                .role(USER)
                .build();
    }

    private Category createCategory() {
        Category category = Category.builder()
                .popularityCount(10)
                .build();

        CategoryTranslation translation = CategoryTranslation.builder()
                .languageCode("en")
                .name("Sci-Fi")
                .description("Science Fiction")
                .category(category)
                .build();
        category.getTranslations().put("en", translation);

        return category;
    }

    private Author createAuthor() {
        Author author = Author.builder()
                .birthYear((short) 1917)
                .deathYear((short) 2008)
                .popularityCount(100)
                .build();

        AuthorTranslation translation = AuthorTranslation.builder()
                .languageCode("en")
                .fullName("Arthur C. Clarke")
                .country("United Kingdom")
                .biography("Famous sci-fi writer")
                .author(author)
                .build();
        author.getTranslations().put("en", translation);

        return author;
    }

    private Book createBook(Category category, int pages) {
        Book book = Book.builder()
                .category(category)
                .publishYear((short) 2010)
                .pages((short) pages)
                .coverImageUrl("http://example.com/cover.png")
                .embedding(new float[384])
                .status(NEW)
                .popularityCount(0)
                .build();

        BookTranslation translation = BookTranslation.builder()
                .languageCode("en")
                .title("A Great Book")
                .bookLanguage("English")
                .description("Book description")
                .book(book)
                .build();
        book.getTranslations().put("en", translation);

        return book;
    }

}

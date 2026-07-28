package org.example.library.book.service;

import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.book.dto.GlobalBookDetails;
import org.example.library.book.dto.LibraryBookDetails;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.book.domain.BookStatus.PRELIMINARY;
import static org.example.library.library_book.domain.LibraryBookStatus.READING;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class BookDetailsServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private BookDetailsService service;

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
    }

    @Test
    void shouldReturnDetailsWithLibraryBookWhenInUserLibrary() {
        var user = saveUser("test@example.com");
        var author = saveAuthor();
        var category = saveCategory();
        var book = saveBook("Book Title", author, category);
        saveLibraryBook(user, book, "User Title", null);

        var details = service.getDetails(book.getId(), user.getId());

        assertThat(details).isInstanceOf(LibraryBookDetails.class);
        var libraryDetails = (LibraryBookDetails) details;
        assertThat(libraryDetails.getLibraryBook()).isNotNull();
        assertThat(libraryDetails.getLibraryBook().getBook().getTitle()).isEqualTo("User Title");
        assertThat(libraryDetails.getAverageRating()).isZero();
        assertThat(libraryDetails.getRatingsNumber()).isZero();
    }

    @Test
    void shouldReturnDetailsWithBookWhenNotInUserLibrary() {
        var user = saveUser("other@example.com");
        var author = saveAuthor();
        var category = saveCategory();
        var book = saveBook("Book Title", author, category);

        var details = service.getDetails(book.getId(), user.getId());

        assertThat(details).isInstanceOf(GlobalBookDetails.class);
        var globalDetails = (GlobalBookDetails) details;
        assertThat(globalDetails.getBook()).isNotNull();
        assertThat(globalDetails.getBook().getTitle()).isEqualTo("Book Title");
    }

    @Test
    void shouldReturnDetailsWithAverageRating() {
        var user1 = saveUser("user1@example.com");
        var user2 = saveUser("user2@example.com");
        var author = saveAuthor();
        var category = saveCategory();
        var book = saveBook("Rated Book", author, category);
        saveLibraryBook(user1, book, "Title 1", (byte) 5);
        saveLibraryBook(user2, book, "Title 2", (byte) 3);

        var details = service.getDetails(book.getId(), user1.getId());

        assertThat(details.getAverageRating()).isEqualTo(4.0);
        assertThat(details.getRatingsNumber()).isEqualTo(2L);
    }

    @Test
    void shouldThrowNotFoundWhenBookDoesNotExist() {
        var user = saveUser("test@example.com");
        var userId = user.getId();

        assertThatThrownBy(() -> service.getDetails(-999, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.book.not_found");
    }

    private User saveUser(String email) {
        var user = User.builder()
                .email(email)
                .fullName("Test User")
                .password("password")
                .role(USER)
                .build();

        testDbClient.saveUser(user);
        return user;
    }

    private Author saveAuthor() {
        var author = Author.builder()
                .birthYear((short) 1900)
                .popularityCount(0)
                .build();

        var translation = AuthorTranslation.builder()
                .languageCode("en")
                .fullName("Author Name")
                .country("Country")
                .author(author)
                .build();
        author.setTranslations(Map.of("en", translation));

        testDbClient.saveAuthor(author);
        return author;
    }

    private Category saveCategory() {
        var category = Category.builder()
                .popularityCount(0)
                .build();

        var translation = CategoryTranslation.builder()
                .languageCode("en")
                .name("Category Name")
                .category(category)
                .build();
        category.setTranslations(Map.of("en", translation));

        testDbClient.saveCategory(category);
        return category;
    }

    private Book saveBook(String title, Author author, Category category) {
        var book = Book.builder()
                .popularityCount(0)
                .status(PRELIMINARY)
                .authors(author != null ? Set.of(author) : Set.of())
                .category(category)
                .build();

        var translation = BookTranslation.builder()
                .languageCode("en")
                .title(title)
                .bookLanguage("en")
                .book(book)
                .build();
        book.setTranslations(Map.of("en", translation));

        testDbClient.saveBook(book);
        if (author != null) {
            testDbClient.linkBookToAuthor(book.getId(), author.getId());
        }

        return book;
    }

    private void saveLibraryBook(User user, Book book, String title, Byte rating) {
        var libraryBook = LibraryBook.builder()
                .user(user)
                .book(book)
                .title(title)
                .status(READING)
                .rating(rating)
                .build();

        testDbClient.saveLibraryBook(libraryBook);
    }

}

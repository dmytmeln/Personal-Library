package org.example.library.quote.service;

import org.example.library.book.domain.Book;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.quote.domain.Quote;
import org.example.library.quote.dto.QuoteRequest;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class QuoteServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private QuoteService service;

    @BeforeEach
    void cleanDbBefore() {
        testDbClient.cleanDatabase();
    }

    @AfterEach
    void tearDownEach() {
        testDbClient.cleanDatabase();
    }

    @Test
    void shouldGetQuotesByLibraryBookId() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(user, book);
        var quote1 = Quote.builder()
                .text("Quote 1")
                .libraryBook(libraryBook)
                .build();
        var quote2 = Quote.builder()
                .text("Quote 2")
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveQuote(quote1);
        testDbClient.saveQuote(quote2);

        var result = service.getByLibraryBookId(libraryBook.getId(), user.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting("text").containsExactlyInAnyOrder("Quote 1", "Quote 2");
    }

    @Test
    void shouldCreateNewQuote() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(user, book);
        var request = new QuoteRequest("New Quote Text", "42", "Good one");

        var result = service.create(libraryBook.getId(), request, user.getId());

        assertThat(result.id()).isNotNull();
        assertThat(result.text()).isEqualTo("New Quote Text");
        assertThat(result.page()).isEqualTo("42");
        assertThat(result.comment()).isEqualTo("Good one");

        var savedQuote = testDbClient.findQuoteById(result.id());
        assertThat(savedQuote).isNotNull();
        assertThat(savedQuote.getText()).isEqualTo("New Quote Text");
        assertThat(savedQuote.getPage()).isEqualTo("42");
        assertThat(savedQuote.getComment()).isEqualTo("Good one");
    }

    @Test
    void shouldUpdateExistingQuote() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(user, book);
        var quote = Quote.builder()
                .text("Old Text")
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveQuote(quote);
        var request = new QuoteRequest("Updated Text", "10", "Updated comment");

        var result = service.update(quote.getId(), request, user.getId());

        assertThat(result.id()).isEqualTo(quote.getId());
        assertThat(result.text()).isEqualTo("Updated Text");

        var updatedQuote = testDbClient.findQuoteById(quote.getId());
        assertThat(updatedQuote).isNotNull();
        assertThat(updatedQuote.getText()).isEqualTo("Updated Text");
        assertThat(updatedQuote.getPage()).isEqualTo("10");
        assertThat(updatedQuote.getComment()).isEqualTo("Updated comment");
    }

    @Test
    void shouldDeleteQuote() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(user, book);
        var quote = Quote.builder()
                .text("To delete")
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveQuote(quote);

        service.delete(quote.getId(), user.getId());

        assertThat(testDbClient.findQuoteById(quote.getId())).isNull();
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingSomeoneElsesQuote() {
        var user1 = saveUser("user1@example.com");
        var user2 = saveUser("user2@example.com");
        var book = saveBook();
        var libraryBook = saveLibraryBook(user1, book);
        var quote = Quote.builder()
                .text("User 1 Quote")
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveQuote(quote);

        var request = new QuoteRequest("Hack", "1", "Hacked");

        assertThatThrownBy(() -> service.update(quote.getId(), request, user2.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.quote.not_found");
    }

    private User saveUser() {
        return saveUser("user@example.com");
    }

    private User saveUser(String email) {
        var user = User.builder()
                .email(email)
                .fullName("User")
                .password("pass")
                .role(USER)
                .build();

        testDbClient.saveUser(user);
        return user;
    }

    private Book saveBook() {
        var book = Book.builder()
                .status(NEW)
                .popularityCount(0)
                .build();

        testDbClient.saveBook(book);
        return book;
    }

    private LibraryBook saveLibraryBook(User user, Book book) {
        var libraryBook = LibraryBook.builder()
                .user(user)
                .book(book)
                .title("Title")
                .status(NO_TAG)
                .build();

        testDbClient.saveLibraryBook(libraryBook);
        return libraryBook;
    }

}

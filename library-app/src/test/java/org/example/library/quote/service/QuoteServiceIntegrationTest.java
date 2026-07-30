package org.example.library.quote.service;

import org.example.library.common.exception.NotFoundException;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.example.library.quote.dto.QuoteRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuoteServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private QuoteService service;

    @Test
    void shouldGetQuotesByLibraryBookId() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
        saveQuote(q -> q.libraryBook(libraryBook).text("Quote 1"));
        saveQuote(q -> q.libraryBook(libraryBook).text("Quote 2"));

        var result = service.getByLibraryBookId(libraryBook.getId(), user.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting("text").containsExactlyInAnyOrder("Quote 1", "Quote 2");
    }

    @Test
    void shouldCreateNewQuote() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
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
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
        var quote = saveQuote(q -> q.libraryBook(libraryBook).text("Old Text"));
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
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
        var quote = saveQuote(q -> q.libraryBook(libraryBook).text("To delete"));

        service.delete(quote.getId(), user.getId());

        assertThat(testDbClient.findQuoteById(quote.getId())).isNull();
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingSomeoneElsesQuote() {
        var user1 = saveUser(u -> u.email("user1@example.com"));
        var user2 = saveUser(u -> u.email("user2@example.com"));
        var book = saveBook();
        var libraryBook = saveLibraryBook(lb -> lb.user(user1).book(book));
        var quote = saveQuote(q -> q.libraryBook(libraryBook).text("User 1 Quote"));

        var request = new QuoteRequest("Hack", "1", "Hacked");

        assertThatThrownBy(() -> service.update(quote.getId(), request, user2.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.quote.not_found");
    }

}

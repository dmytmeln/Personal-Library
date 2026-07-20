package org.example.library.quote.repository;

import org.example.library.book.domain.Book;
import org.example.library.config.AbstractRepositoryTest;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.quote.domain.Quote;
import org.example.library.user.domain.User;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.config.EntityRecursiveComparisonConfigs.LIBRARY_BOOK_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.QUOTE_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.QUOTE_SAVED;
import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;
import static org.example.library.user.domain.Role.USER;

class QuoteRepositoryTest extends AbstractRepositoryTest<QuoteRepository> {

    @Test
    void save_ShouldPersistQuote_AndNotCascadeLibraryBookOrUser() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        long initialUserCount = testDbClient.countUsers();
        long initialLibraryBookCount = testDbClient.countLibraryBooks();
        Quote expected = createQuote(libraryBook);

        Quote actual = repository.save(expected);

        assertThat(actual)
                .usingRecursiveComparison(QUOTE_SAVED)
                .isEqualTo(expected);
        Quote dbState = testDbClient.findQuoteById(actual.getId());
        assertThat(dbState)
                .isNotNull()
                .usingRecursiveComparison(QUOTE_DIRECT_FIELDS)
                .isEqualTo(actual);
        assertThat(testDbClient.countUsers()).isEqualTo(initialUserCount);
        assertThat(testDbClient.countLibraryBooks()).isEqualTo(initialLibraryBookCount);
    }

    @Test
    @Transactional
    void findById_ShouldReturnQuote_WhenExists() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Quote quote = createQuote(libraryBook);
        testDbClient.saveQuote(quote);

        Optional<Quote> actual = repository.findById(quote.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(QUOTE_DIRECT_FIELDS)
                .isEqualTo(quote);
        assertThat(actual.get().getLibraryBook())
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(LIBRARY_BOOK_DIRECT_FIELDS)
                .isEqualTo(libraryBook);
    }

    @Test
    void delete_ShouldRemoveQuote_ButKeepLibraryBook() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Quote quote = createQuote(libraryBook);
        testDbClient.saveQuote(quote);
        long initialLibraryBookCount = testDbClient.countLibraryBooks();

        repository.delete(quote);

        assertThat(testDbClient.findQuoteById(quote.getId())).isNull();
        assertThat(testDbClient.countLibraryBooks()).isEqualTo(initialLibraryBookCount);
    }

    @Test
    void findByLibraryBookIdAndLibraryBookUserIdOrderByCreatedAtDesc_ShouldReturnQuotesOrdered() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Quote quote1 = createQuote(libraryBook);
        quote1.setCreatedAt(LocalDateTime.now().minusDays(1));
        testDbClient.saveQuote(quote1);
        Quote quote2 = createQuote(libraryBook);
        quote2.setCreatedAt(LocalDateTime.now());
        testDbClient.saveQuote(quote2);

        List<Quote> actual = repository.findByLibraryBookIdAndLibraryBookUserIdOrderByCreatedAtDesc(libraryBook.getId(), user.getId());

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getId()).isEqualTo(quote2.getId());
        assertThat(actual.get(1).getId()).isEqualTo(quote1.getId());
    }

    @Test
    void findByIdAndLibraryBookUserId_ShouldReturnQuote() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Quote quote = createQuote(libraryBook);
        testDbClient.saveQuote(quote);

        Optional<Quote> actual = repository.findByIdAndLibraryBookUserId(quote.getId(), user.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(quote.getId());
    }

    @Test
    void findByIdAndLibraryBookUserId_ShouldReturnEmpty_WhenDoesNotExist() {
        Optional<Quote> actual = repository.findByIdAndLibraryBookUserId(999, 999);

        assertThat(actual).isEmpty();
    }

    private User createUser(String email) {
        return User.builder()
                .email(email)
                .fullName("Quote Owner")
                .password("password")
                .role(USER)
                .build();
    }

    private Book createBook() {
        return Book.builder()
                .publishYear((short) 2010)
                .pages((short) 250)
                .coverImageUrl("http://example.com/cover.png")
                .popularityCount(0)
                .build();
    }

    private LibraryBook createLibraryBook(Book book, User user) {
        return LibraryBook.builder()
                .book(book)
                .user(user)
                .status(NO_TAG)
                .build();
    }

    private Quote createQuote(LibraryBook libraryBook) {
        return Quote.builder()
                .libraryBook(libraryBook)
                .text("This is a wise quote.")
                .page("42")
                .comment("A thought-provoking comment.")
                .build();
    }
}

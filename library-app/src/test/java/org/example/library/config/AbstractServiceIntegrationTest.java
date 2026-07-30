package org.example.library.config;

import org.example.library.auth.domain.RefreshToken;
import org.example.library.author.domain.Author;
import org.example.library.book.domain.Book;
import org.example.library.category.domain.Category;
import org.example.library.collection.domain.Collection;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.note.domain.Note;
import org.example.library.quote.domain.Quote;
import org.example.library.reading_goal.domain.ReadingGoal;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Locale;
import java.util.function.Consumer;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
public abstract class AbstractServiceIntegrationTest {

    @Autowired
    protected TestDbClient testDbClient;

    @BeforeEach
    void setUp() {
        testDbClient.cleanDatabase();
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    protected Author saveAuthor() {
        return saveAuthor(c -> {
        });
    }

    protected Author saveAuthor(Consumer<AuthorConfigurer> customizer) {
        var configurer = new AuthorConfigurer(testDbClient);
        customizer.accept(configurer);
        return configurer.save();
    }

    protected Book saveBook() {
        return saveBook(c -> {
        });
    }

    protected Book saveBook(Consumer<BookConfigurer> customizer) {
        var configurer = new BookConfigurer(testDbClient);
        customizer.accept(configurer);
        return configurer.save();
    }

    protected Category saveCategory() {
        return saveCategory(c -> {
        });
    }

    protected Category saveCategory(Consumer<CategoryConfigurer> customizer) {
        var configurer = new CategoryConfigurer(testDbClient);
        customizer.accept(configurer);
        return configurer.save();
    }

    protected User saveUser() {
        return saveUser(c -> {
        });
    }

    protected User saveUser(Consumer<UserConfigurer> customizer) {
        var configurer = new UserConfigurer(testDbClient);
        customizer.accept(configurer);
        return configurer.save();
    }

    protected LibraryBook saveLibraryBook() {
        return saveLibraryBook(c -> {
        });
    }

    protected LibraryBook saveLibraryBook(Consumer<LibraryBookConfigurer> customizer) {
        var configurer = new LibraryBookConfigurer(testDbClient);
        customizer.accept(configurer);
        return configurer.save();
    }

    protected Collection saveCollection() {
        return saveCollection(c -> {
        });
    }

    protected Collection saveCollection(Consumer<CollectionConfigurer> customizer) {
        var configurer = new CollectionConfigurer(testDbClient);
        customizer.accept(configurer);
        return configurer.save();
    }

    protected CollectionBook saveCollectionBook() {
        return saveCollectionBook(c -> {
        });
    }

    protected CollectionBook saveCollectionBook(Consumer<CollectionBookConfigurer> customizer) {
        var configurer = new CollectionBookConfigurer(testDbClient);
        customizer.accept(configurer);
        return configurer.save();
    }

    protected Note saveNote() {
        return saveNote(c -> {
        });
    }

    protected Note saveNote(Consumer<NoteConfigurer> customizer) {
        var configurer = new NoteConfigurer(testDbClient);
        customizer.accept(configurer);
        return configurer.save();
    }

    protected Quote saveQuote() {
        return saveQuote(c -> {
        });
    }

    protected Quote saveQuote(Consumer<QuoteConfigurer> customizer) {
        var configurer = new QuoteConfigurer(testDbClient);
        customizer.accept(configurer);
        return configurer.save();
    }

    protected ReadingGoal saveReadingGoal() {
        return saveReadingGoal(c -> {
        });
    }

    protected ReadingGoal saveReadingGoal(Consumer<ReadingGoalConfigurer> customizer) {
        var configurer = new ReadingGoalConfigurer(testDbClient);
        customizer.accept(configurer);
        return configurer.save();
    }

    protected RefreshToken saveRefreshToken() {
        return saveRefreshToken(c -> {
        });
    }

    protected RefreshToken saveRefreshToken(Consumer<RefreshTokenConfigurer> customizer) {
        var configurer = new RefreshTokenConfigurer(testDbClient);
        customizer.accept(configurer);
        return configurer.save();
    }

}

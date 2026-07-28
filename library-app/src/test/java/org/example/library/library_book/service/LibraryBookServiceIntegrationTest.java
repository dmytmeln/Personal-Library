package org.example.library.library_book.service;

import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.collection.domain.Collection;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.example.library.common.pagination.PaginationParams;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.domain.LibraryBookStatus;
import org.example.library.library_book.dto.CreateLocalBookDto;
import org.example.library.library_book.dto.LibraryBookSearchCriteria;
import org.example.library.library_book.repository.LibraryBookRepository;
import org.example.library.note.domain.Note;
import org.example.library.quote.domain.Quote;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.book.domain.BookStatus.SYNCED;
import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;
import static org.example.library.library_book.domain.LibraryBookStatus.READ;
import static org.example.library.library_book.domain.LibraryBookStatus.READING;
import static org.example.library.library_book.domain.LibraryBookStatus.TO_READ;
import static org.example.library.note.domain.Note.NoteType.TEXT;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class LibraryBookServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private LibraryBookRepository repository;

    @Autowired
    private LibraryBookService service;

    private User defaultUser;

    private Category defaultCategory;

    @BeforeAll
    static void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @BeforeEach
    void cleanDbBefore() {
        testDbClient.cleanDatabase();
        defaultUser = saveUser();
        defaultCategory = saveCategory();
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
    void shouldGetAllByUserId() {
        var book = saveBook("Global Book");
        saveLibraryBook(book, defaultUser);
        var criteria = new LibraryBookSearchCriteria();
        var pagination = new PaginationParams();

        var result = service.getAllByUserId(defaultUser.getId(), criteria, pagination);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBook().getTitle()).isEqualTo("Global Book");
    }

    @Test
    void shouldCreateLocalBook() {
        var dto = CreateLocalBookDto.builder()
                .title("Local Book")
                .description("Local Description")
                .bookLanguage("uk")
                .status(READING)
                .build();

        service.createLocalBook(dto, defaultUser.getId());

        var libraryBooks = repository.findAllByUserIdWithBook(defaultUser.getId());
        assertThat(libraryBooks).hasSize(1);
        var saved = libraryBooks.get(0);
        assertThat(saved.getTitle()).isEqualTo("Local Book");
        assertThat(saved.getStatus()).isEqualTo(READING);
        assertThat(saved.getBook().getOwner().getId()).isEqualTo(defaultUser.getId());
    }

    @Test
    void shouldAddExistingBookToLibrary() {
        var book = saveBook("Existing Book");

        service.create(book.getId(), defaultUser.getId());

        var libraryBooks = repository.findAllByUserIdWithBook(defaultUser.getId());
        assertThat(libraryBooks).hasSize(1);
        assertThat(libraryBooks.get(0).getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    void shouldRateLibraryBook() {
        var book = saveBook("Book to Rate");
        var libraryBook = saveLibraryBook(book, defaultUser);

        var result = service.rate(libraryBook.getId(), defaultUser.getId(), 5);

        assertThat(result.getRating()).isEqualTo((byte) 5);
        var updated = testDbClient.findLibraryBookById(libraryBook.getId());
        assertThat(updated).isNotNull();
        assertThat(updated.getRating()).isEqualTo((byte) 5);
    }

    @Test
    void shouldUpdateStatus() {
        var book = saveBook("Book Status");
        var libraryBook = saveLibraryBook(book, defaultUser, NO_TAG);

        var result = service.updateStatus(libraryBook.getId(), defaultUser.getId(), READ);

        assertThat(result.getStatus()).isEqualTo(READ.name());
        var updated = testDbClient.findLibraryBookById(libraryBook.getId());
        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(READ);
    }

    @Test
    void shouldDeleteLibraryBook() {
        var book = saveBook("Book to Delete");
        var libraryBook = saveLibraryBook(book, defaultUser);

        service.delete(libraryBook.getId(), defaultUser.getId());

        assertThat(testDbClient.findLibraryBookById(libraryBook.getId())).isNull();
    }

    @Test
    void shouldBulkAddBooks() {
        var book1 = saveBook("Book 1");
        var book2 = saveBook("Book 2");
        var book3 = saveBook("Book 3");
        saveLibraryBook(book1, defaultUser);

        service.bulkAdd(List.of(book1.getId(), book2.getId(), book3.getId()), defaultUser.getId());

        var libraryBooks = repository.findAllByUserId(defaultUser.getId());
        assertThat(libraryBooks).hasSize(3);
        var bookIds = libraryBooks.stream().map(lb -> lb.getBook().getId()).toList();
        assertThat(bookIds).containsExactlyInAnyOrder(book1.getId(), book2.getId(), book3.getId());
    }

    @Test
    void shouldBulkUpdateStatus() {
        var lb1 = saveLibraryBook(saveBook("B1"), defaultUser, TO_READ);
        var lb2 = saveLibraryBook(saveBook("B2"), defaultUser, READING);

        service.bulkUpdateStatus(List.of(lb1.getId(), lb2.getId()), defaultUser.getId(), READ);

        var updatedLb1 = testDbClient.findLibraryBookById(lb1.getId());
        var updatedLb2 = testDbClient.findLibraryBookById(lb2.getId());
        assertThat(updatedLb1).isNotNull();
        assertThat(updatedLb2).isNotNull();
        assertThat(updatedLb1.getStatus()).isEqualTo(READ);
        assertThat(updatedLb1.getFinishedAt()).isEqualTo(LocalDate.now());
        assertThat(updatedLb2.getStatus()).isEqualTo(READ);
        assertThat(updatedLb2.getFinishedAt()).isEqualTo(LocalDate.now());

        service.bulkUpdateStatus(List.of(lb1.getId()), defaultUser.getId(), READING);
        updatedLb1 = testDbClient.findLibraryBookById(lb1.getId());
        assertThat(updatedLb1).isNotNull();
        assertThat(updatedLb1.getStatus()).isEqualTo(READING);
        assertThat(updatedLb1.getFinishedAt()).isNull();
    }

    @Test
    void shouldBulkDeleteBooks() {
        var lb1 = saveLibraryBook(saveBook("B1"), defaultUser);
        var lb2 = saveLibraryBook(saveBook("B2"), defaultUser);

        service.bulkDelete(List.of(lb1.getId(), lb2.getId()), defaultUser.getId());

        assertThat(testDbClient.findLibraryBookById(lb1.getId())).isNull();
        assertThat(testDbClient.findLibraryBookById(lb2.getId())).isNull();
    }

    @Test
    void shouldSearchByMoodWithoutStatusFilter() {
        float[] v1 = new float[384];
        v1[0] = 0.9f;
        saveLibraryBook(saveBook("Space Adventure", v1), defaultUser, READ);

        float[] v2 = new float[384];
        v2[0] = 0.8f;
        saveLibraryBook(saveBook("Galactic Journey", v2), defaultUser, TO_READ);

        float[] v3 = new float[384];
        v3[1] = 0.9f;
        saveLibraryBook(saveBook("Historical Romance", v3), defaultUser, TO_READ);

        float[] v4 = new float[384];
        v4[1] = 0.8f;
        saveLibraryBook(saveBook("Medieval Love", v4), defaultUser, READING);

        float[] v5 = new float[384];
        v5[5] = 0.9f;
        saveLibraryBook(saveBook("Cooking Basics", v5), defaultUser, TO_READ);

        var results = service.searchByMood("space trip", null, defaultUser.getId(), 2);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getBook().getTitle()).containsAnyOf("Space Adventure", "Galactic Journey");
        assertThat(results.get(1).getBook().getTitle()).containsAnyOf("Space Adventure", "Galactic Journey");
    }

    @Test
    void shouldSearchByMoodWithStatusFilter() {
        float[] v1 = new float[384];
        v1[0] = 0.9f;
        saveLibraryBook(saveBook("Space Adventure", v1), defaultUser, READ);

        float[] v2 = new float[384];
        v2[0] = 0.8f;
        saveLibraryBook(saveBook("Galactic Journey", v2), defaultUser, TO_READ);

        float[] v3 = new float[384];
        v3[1] = 0.9f;
        saveLibraryBook(saveBook("Historical Romance", v3), defaultUser, TO_READ);

        float[] v4 = new float[384];
        v4[1] = 0.8f;
        saveLibraryBook(saveBook("Medieval Love", v4), defaultUser, READING);

        float[] v5 = new float[384];
        v5[5] = 0.9f;
        saveLibraryBook(saveBook("Cooking Basics", v5), defaultUser, TO_READ);

        var results = service.searchByMood("space trip", TO_READ, defaultUser.getId(), 2);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getBook().getTitle()).isEqualTo("Galactic Journey");
        assertThat(results.stream().anyMatch(r -> r.getBook().getTitle().equals("Space Adventure"))).isFalse();
    }

    @Test
    void shouldCascadeDeleteDependencies() {
        var book = saveBook("Cascade Test Book");
        var libraryBook = saveLibraryBook(book, defaultUser);
        var collection = Collection.builder()
                .name("Test Collection")
                .user(defaultUser)
                .build();
        testDbClient.saveCollection(collection);

        var collectionBook = CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook.getId()))
                .collection(collection)
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveCollectionBook(collectionBook);

        var note = Note.builder()
                .libraryBook(libraryBook)
                .content("Test Note")
                .noteType(TEXT)
                .build();
        testDbClient.saveNote(note);

        var quote = Quote.builder()
                .libraryBook(libraryBook)
                .text("Test Quote 1")
                .build();
        testDbClient.saveQuote(quote);

        service.delete(libraryBook.getId(), defaultUser.getId());

        assertThat(testDbClient.findLibraryBookById(libraryBook.getId())).isNull();
        assertThat(testDbClient.countNotes()).isZero();
        assertThat(testDbClient.countQuotes()).isZero();
        assertThat(testDbClient.findCollectionBookById(collection.getId(), libraryBook.getId())).isNull();
    }

    private User saveUser() {
        var user = User.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password")
                .role(USER)
                .build();

        testDbClient.saveUser(user);
        return user;
    }

    private Category saveCategory() {
        var category = Category.builder()
                .popularityCount(0)
                .build();

        var translation = CategoryTranslation.builder()
                .languageCode("en")
                .name("Default Category")
                .description("Description")
                .category(category)
                .build();
        category.setTranslations(new HashMap<>(Map.of("en", translation)));

        testDbClient.saveCategory(category);
        return category;
    }

    private Book saveBook(String title) {
        return saveBook(title, null);
    }

    private Book saveBook(String title, float[] embedding) {
        var book = Book.builder()
                .category(defaultCategory)
                .owner(null)
                .status(SYNCED)
                .embedding(embedding)
                .popularityCount(0)
                .authors(Set.of())
                .build();

        var translation = BookTranslation.builder()
                .languageCode("en")
                .title(title)
                .bookLanguage("en")
                .description("Description")
                .book(book)
                .build();
        book.setTranslations(new HashMap<>(Map.of("en", translation)));

        testDbClient.saveBook(book);
        return book;
    }

    private LibraryBook saveLibraryBook(Book book, User user) {
        return saveLibraryBook(book, user, TO_READ);
    }

    private LibraryBook saveLibraryBook(Book book, User user, LibraryBookStatus status) {
        var libraryBook = LibraryBook.of(book, user);
        libraryBook.setStatus(status);

        testDbClient.saveLibraryBook(libraryBook);
        return libraryBook;
    }

}

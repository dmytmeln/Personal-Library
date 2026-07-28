package org.example.library.collection_book.service;

import org.example.library.book.domain.Book;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.collection.domain.Collection;
import org.example.library.collection_book.dto.CollectionBookSearchParams;
import org.example.library.common.exception.BadRequestException;
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
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.example.library.library_book.domain.LibraryBookStatus.TO_READ;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class CollectionBookServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private CollectionBookService service;

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
    void shouldAddBookToCollection() {
        var user = saveUser("user@example.com");
        var book = saveBook();
        var libraryBook = saveLibraryBook(user, book);
        var collection = saveCollection(user, "My Collection");

        service.addBookToCollection(user.getId(), collection.getId(), libraryBook.getId());

        assertThat(testDbClient.findCollectionBookById(collection.getId(), libraryBook.getId())).isNotNull();
    }

    @Test
    void shouldThrowBadRequestWhenAddingToAnotherUsersCollection() {
        var user1 = saveUser("user1@example.com");
        var user2 = saveUser("user2@example.com");
        var book = saveBook();
        var libraryBook = saveLibraryBook(user1, book);
        var collection = saveCollection(user2, "User 2 Collection");

        assertThatThrownBy(() -> service.addBookToCollection(user1.getId(), collection.getId(), libraryBook.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.collection.not_belong_to_user");
    }

    @Test
    void shouldBulkAddBooksToCollection() {
        var user = saveUser("user@example.com");
        var book1 = saveBook();
        var book2 = saveBook();
        var lb1 = saveLibraryBook(user, book1);
        var lb2 = saveLibraryBook(user, book2);
        var collection = saveCollection(user, "Bulk Collection");

        service.bulkAddBooksToCollection(user.getId(), collection.getId(), List.of(lb1.getId(), lb2.getId()));

        assertThat(testDbClient.findCollectionBookById(collection.getId(), lb1.getId())).isNotNull();
        assertThat(testDbClient.findCollectionBookById(collection.getId(), lb2.getId())).isNotNull();
    }

    @Test
    void shouldGetCollectionBooksPaginated() {
        var user = saveUser("user@example.com");
        var book1 = saveBook();
        var book2 = saveBook();
        var lb1 = saveLibraryBook(user, book1);
        var lb2 = saveLibraryBook(user, book2);
        var collection = saveCollection(user, "Paginated Collection");
        service.addBookToCollection(user.getId(), collection.getId(), lb1.getId());
        service.addBookToCollection(user.getId(), collection.getId(), lb2.getId());

        var searchParams = new CollectionBookSearchParams();
        var paginationParams = new PaginationParams();
        paginationParams.setPage(0);
        paginationParams.setSize(10);

        var result = service.getCollectionBooksPaginated(user.getId(), collection.getId(), searchParams, paginationParams);

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldRemoveBookFromCollection() {
        var user = saveUser("user@example.com");
        var book = saveBook();
        var lb = saveLibraryBook(user, book);
        var collection = saveCollection(user, "Removal Collection");
        service.addBookToCollection(user.getId(), collection.getId(), lb.getId());

        service.removeBookFromCollection(user.getId(), collection.getId(), lb.getId());

        assertThat(testDbClient.findCollectionBookById(collection.getId(), lb.getId())).isNull();
    }

    @Test
    void shouldBulkRemoveBooksFromCollection() {
        var user = saveUser("user@example.com");
        var lb1 = saveLibraryBook(user, saveBook());
        var lb2 = saveLibraryBook(user, saveBook());
        var collection = saveCollection(user, "Bulk Removal");
        service.bulkAddBooksToCollection(user.getId(), collection.getId(), List.of(lb1.getId(), lb2.getId()));

        service.bulkRemoveBooksFromCollection(user.getId(), collection.getId(), List.of(lb1.getId(), lb2.getId()));

        assertThat(testDbClient.countCollectionBooks()).isZero();
    }

    @Test
    void shouldRemoveBookFromAllCollections() {
        var user = saveUser("user@example.com");
        var lb = saveLibraryBook(user, saveBook());
        var col1 = saveCollection(user, "Col 1");
        var col2 = saveCollection(user, "Col 2");
        service.addBookToCollection(user.getId(), col1.getId(), lb.getId());
        service.addBookToCollection(user.getId(), col2.getId(), lb.getId());

        service.removeBookFromAllCollections(user.getId(), lb.getId());

        assertThat(testDbClient.countCollectionBooks()).isZero();
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

    private Category saveCategory() {
        var category = Category.builder()
                .popularityCount(0)
                .build();

        var translation = CategoryTranslation.builder()
                .languageCode("en")
                .name("Default Category")
                .description("Description of Default Category")
                .category(category)
                .build();
        category.setTranslations(new HashMap<>(Map.of("en", translation)));

        testDbClient.saveCategory(category);
        return category;
    }

    private Book saveBook() {
        var category = saveCategory();

        var book = Book.builder()
                .category(category)
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
                .status(TO_READ)
                .title("Library Book")
                .build();

        testDbClient.saveLibraryBook(libraryBook);
        return libraryBook;
    }

    private Collection saveCollection(User user, String name) {
        var collection = Collection.builder()
                .user(user)
                .name(name)
                .build();

        testDbClient.saveCollection(collection);
        return collection;
    }

}

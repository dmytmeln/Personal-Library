package org.example.library.collection.service;

import org.example.library.book.domain.Book;
import org.example.library.collection.domain.Collection;
import org.example.library.collection.dto.CreateCollectionRequest;
import org.example.library.collection.dto.UpdateCollectionDto;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
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

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.book.domain.BookStatus.PRELIMINARY;
import static org.example.library.library_book.domain.LibraryBookStatus.TO_READ;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class CollectionServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private CollectionService service;

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
    void shouldReturnAllCollectionsForUser() {
        var user = saveUser();
        saveCollection("Collection 1", user);
        saveCollection("Collection 2", user);

        var result = service.getAllCollections(user.getId(), null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("Collection 1", "Collection 2");
    }

    @Test
    void shouldReturnCollectionsByUserIdAndBookId() {
        var user = saveUser();
        var collection = saveCollection("My Collection", user);
        var book = saveBook();
        var libraryBook = saveLibraryBook(book, user);
        saveCollectionBook(collection, libraryBook);

        var result = service.getAllByUserIdAndBookId(user.getId(), book.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("My Collection");
    }

    @Test
    void shouldReturnUserCollectionTree() {
        var user = saveUser();
        var root = saveCollection("Root", user);
        var child = Collection.builder()
                .name("Child")
                .user(user)
                .parent(root)
                .build();
        testDbClient.saveCollection(child);

        var result = service.getUserCollectionTree(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Root");
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getName()).isEqualTo("Child");
    }

    @Test
    void shouldReturnCollectionDetailsWithAncestors() {
        var user = saveUser();
        var root = saveCollection("Root", user);
        var child = Collection.builder()
                .name("Child")
                .user(user)
                .parent(root)
                .build();
        testDbClient.saveCollection(child);

        var result = service.getCollectionDetails(child.getId(), user.getId());

        assertThat(result.getName()).isEqualTo("Child");
        assertThat(result.getAncestors()).hasSize(1);
        assertThat(result.getAncestors().get(0).getName()).isEqualTo("Root");
    }

    @Test
    void shouldThrowNotFoundWhenGettingNonExistentCollectionDetails() {
        var user = saveUser();

        assertThatThrownBy(() -> service.getCollectionDetails(-1, user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.collection.not_found");
    }

    @Test
    void shouldCreateCollection() {
        var user = saveUser();
        var request = new CreateCollectionRequest();
        request.setName("New Collection");
        request.setDescription("Description");

        var result = service.createCollection(request, user.getId());

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("New Collection");
        assertThat(testDbClient.findCollectionById(result.getId())).isNotNull();
    }

    @Test
    void shouldCreateSubCollection() {
        var user = saveUser();
        var parent = saveCollection("Parent", user);
        var request = new CreateCollectionRequest();
        request.setName("Sub");
        request.setParentId(parent.getId());

        var result = service.createCollection(request, user.getId());

        assertThat(result.getName()).isEqualTo("Sub");
        var saved = testDbClient.findCollectionById(result.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getParent().getId()).isEqualTo(parent.getId());
    }

    @Test
    void shouldThrowBadRequestWhenCreatingCollectionExceedingMaxDepth() {
        var user = saveUser();
        var c1 = saveCollection("c1", user);
        var c2 = Collection.builder().name("c2").user(user).parent(c1).build();
        testDbClient.saveCollection(c2);
        var c3 = Collection.builder().name("c3").user(user).parent(c2).build();
        testDbClient.saveCollection(c3);
        var c4 = Collection.builder().name("c4").user(user).parent(c3).build();
        testDbClient.saveCollection(c4);

        var request = new CreateCollectionRequest();
        request.setName("c5");
        request.setParentId(c4.getId());

        assertThatThrownBy(() -> service.createCollection(request, user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.collection.max_depth_exceeded");
    }

    @Test
    void shouldUpdateCollection() {
        var user = saveUser();
        var collection = saveCollection("Old Name", user);
        var dto = new UpdateCollectionDto();
        dto.setName("New Name");

        var result = service.updateCollection(collection.getId(), dto, user.getId());

        assertThat(result.getName()).isEqualTo("New Name");
        var saved = testDbClient.findCollectionById(collection.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Name");
    }

    @Test
    void shouldMoveCollection() {
        var user = saveUser();
        var parent1 = saveCollection("Parent 1", user);
        var parent2 = saveCollection("Parent 2", user);
        var child = Collection.builder().name("Child").user(user).parent(parent1).build();
        testDbClient.saveCollection(child);

        service.moveCollection(child.getId(), parent2.getId(), user.getId());

        var saved = testDbClient.findCollectionById(child.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getParent().getId()).isEqualTo(parent2.getId());
    }

    @Test
    void shouldMakeCollectionRootWhenMovingToNullParent() {
        var user = saveUser();
        var parent = saveCollection("Parent", user);
        var child = Collection.builder().name("Child").user(user).parent(parent).build();
        testDbClient.saveCollection(child);

        service.moveCollection(child.getId(), null, user.getId());

        var saved = testDbClient.findCollectionById(child.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getParent()).isNull();
    }

    @Test
    void shouldThrowBadRequestWhenMovingCollectionToItself() {
        var user = saveUser();
        var collection = saveCollection("Collection", user);

        assertThatThrownBy(() -> service.moveCollection(collection.getId(), collection.getId(), user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.collection.cannot_be_own_parent");
    }

    @Test
    void shouldDeleteCollection() {
        var user = saveUser();
        var collection = saveCollection("To Delete", user);

        service.deleteCollection(collection.getId(), user.getId());

        assertThat(testDbClient.findCollectionById(collection.getId())).isNull();
    }

    @Test
    void shouldMoveBookBetweenCollections() {
        var user = saveUser();
        var source = saveCollection("Source", user);
        var target = saveCollection("Target", user);
        var book = saveBook();
        var libraryBook = saveLibraryBook(book, user);
        saveCollectionBook(source, libraryBook);

        service.moveBook(source.getId(), target.getId(), libraryBook.getId(), user.getId());

        assertThat(testDbClient.findCollectionBookById(source.getId(), libraryBook.getId())).isNull();
        assertThat(testDbClient.findCollectionBookById(target.getId(), libraryBook.getId())).isNotNull();
    }

    private User saveUser() {
        var user = User.builder()
                .email("user@test.com")
                .fullName("Test User")
                .password("password")
                .role(USER)
                .build();

        testDbClient.saveUser(user);
        return user;
    }

    private Collection saveCollection(String name, User user) {
        var collection = Collection.builder()
                .name(name)
                .user(user)
                .build();

        testDbClient.saveCollection(collection);
        return collection;
    }

    private Book saveBook() {
        var book = Book.builder()
                .status(PRELIMINARY)
                .popularityCount(0)
                .build();

        testDbClient.saveBook(book);
        return book;
    }

    private LibraryBook saveLibraryBook(Book book, User user) {
        var libraryBook = LibraryBook.builder()
                .book(book)
                .user(user)
                .status(TO_READ)
                .title("Test Library Book")
                .build();

        testDbClient.saveLibraryBook(libraryBook);
        return libraryBook;
    }

    private void saveCollectionBook(Collection collection, LibraryBook libraryBook) {
        var collectionBook = CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook.getId()))
                .collection(collection)
                .libraryBook(libraryBook)
                .build();

        testDbClient.saveCollectionBook(collectionBook);
    }

}

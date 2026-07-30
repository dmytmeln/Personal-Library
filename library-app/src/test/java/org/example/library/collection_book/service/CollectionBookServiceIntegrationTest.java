package org.example.library.collection_book.service;

import org.example.library.collection_book.dto.CollectionBookSearchParams;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.pagination.PaginationParams;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.library_book.domain.LibraryBookStatus.TO_READ;

class CollectionBookServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private CollectionBookService service;

    @Test
    void shouldAddBookToCollection() {
        var user = saveUser(u -> u.email("user@example.com"));
        var book = saveBook(b -> b.title("Library Book").bookLanguage("English"));
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book).status(TO_READ));
        var collection = saveCollection(c -> c.user(user).name("My Collection"));

        service.addBookToCollection(user.getId(), collection.getId(), libraryBook.getId());

        assertThat(testDbClient.findCollectionBookById(collection.getId(), libraryBook.getId())).isNotNull();
    }

    @Test
    void shouldThrowBadRequestWhenAddingToAnotherUsersCollection() {
        var user1 = saveUser(u -> u.email("user1@example.com"));
        var user2 = saveUser(u -> u.email("user2@example.com"));
        var book = saveBook(b -> b.title("Library Book").bookLanguage("English"));
        var libraryBook = saveLibraryBook(lb -> lb.user(user1).book(book).status(TO_READ));
        var collection = saveCollection(c -> c.user(user2).name("User 2 Collection"));

        assertThatThrownBy(() -> service.addBookToCollection(user1.getId(), collection.getId(), libraryBook.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.collection.not_belong_to_user");
    }

    @Test
    void shouldBulkAddBooksToCollection() {
        var user = saveUser(u -> u.email("user@example.com"));
        var book1 = saveBook(b -> b.title("Library Book").bookLanguage("English"));
        var book2 = saveBook(b -> b.title("Library Book").bookLanguage("English"));
        var lb1 = saveLibraryBook(lb -> lb.user(user).book(book1).status(TO_READ));
        var lb2 = saveLibraryBook(lb -> lb.user(user).book(book2).status(TO_READ));
        var collection = saveCollection(c -> c.user(user).name("Bulk Collection"));

        service.bulkAddBooksToCollection(user.getId(), collection.getId(), List.of(lb1.getId(), lb2.getId()));

        assertThat(testDbClient.findCollectionBookById(collection.getId(), lb1.getId())).isNotNull();
        assertThat(testDbClient.findCollectionBookById(collection.getId(), lb2.getId())).isNotNull();
    }

    @Test
    void shouldGetCollectionBooksPaginated() {
        var user = saveUser(u -> u.email("user@example.com"));
        var book1 = saveBook(b -> b.title("Library Book").bookLanguage("English"));
        var book2 = saveBook(b -> b.title("Library Book").bookLanguage("English"));
        var lb1 = saveLibraryBook(lb -> lb.user(user).book(book1).status(TO_READ));
        var lb2 = saveLibraryBook(lb -> lb.user(user).book(book2).status(TO_READ));
        var collection = saveCollection(c -> c.user(user).name("Paginated Collection"));
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
        var user = saveUser(u -> u.email("user@example.com"));
        var book = saveBook(b -> b.title("Library Book").bookLanguage("English"));
        var lb = saveLibraryBook(lbc -> lbc.user(user).book(book).status(TO_READ));
        var collection = saveCollection(c -> c.user(user).name("Removal Collection"));
        service.addBookToCollection(user.getId(), collection.getId(), lb.getId());

        service.removeBookFromCollection(user.getId(), collection.getId(), lb.getId());

        assertThat(testDbClient.findCollectionBookById(collection.getId(), lb.getId())).isNull();
    }

    @Test
    void shouldBulkRemoveBooksFromCollection() {
        var user = saveUser(u -> u.email("user@example.com"));
        var lb1 = saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("Library Book").bookLanguage("English"))).status(TO_READ));
        var lb2 = saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("Library Book").bookLanguage("English"))).status(TO_READ));
        var collection = saveCollection(c -> c.user(user).name("Bulk Removal"));
        service.bulkAddBooksToCollection(user.getId(), collection.getId(), List.of(lb1.getId(), lb2.getId()));

        service.bulkRemoveBooksFromCollection(user.getId(), collection.getId(), List.of(lb1.getId(), lb2.getId()));

        assertThat(testDbClient.countCollectionBooks()).isZero();
    }

    @Test
    void shouldRemoveBookFromAllCollections() {
        var user = saveUser(u -> u.email("user@example.com"));
        var lb = saveLibraryBook(lbc -> lbc.user(user).book(saveBook(b -> b.title("Library Book").bookLanguage("English"))).status(TO_READ));
        var col1 = saveCollection(c -> c.user(user).name("Col 1"));
        var col2 = saveCollection(c -> c.user(user).name("Col 2"));
        service.addBookToCollection(user.getId(), col1.getId(), lb.getId());
        service.addBookToCollection(user.getId(), col2.getId(), lb.getId());

        service.removeBookFromAllCollections(user.getId(), lb.getId());

        assertThat(testDbClient.countCollectionBooks()).isZero();
    }

}

package org.example.library.collection_book.repository;

import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.collection.domain.Collection;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.example.library.config.AbstractRepositoryTest;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.user.domain.User;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.example.library.config.EntityRecursiveComparisonConfigs.COLLECTION_BOOK_SAVED;
import static org.example.library.config.EntityRecursiveComparisonConfigs.COLLECTION_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.LIBRARY_BOOK_DIRECT_FIELDS;
import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;
import static org.example.library.user.domain.Role.USER;

class CollectionBookRepositoryTest extends AbstractRepositoryTest<CollectionBookRepository> {

    @Test
    void save_ShouldPersistCollectionBook_AndNotCascadeCollectionOrLibraryBook() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Collection collection = createCollection(user);
        testDbClient.saveCollection(collection);
        long initialCollectionCount = testDbClient.countCollections();
        long initialLibraryBookCount = testDbClient.countLibraryBooks();
        CollectionBookId collectionBookId = new CollectionBookId(collection.getId(), libraryBook.getId());

        CollectionBook actual = transactionTemplate.execute(status -> {
            Collection collectionRef = entityManager.getReference(Collection.class, collection.getId());
            LibraryBook libraryBookRef = entityManager.getReference(LibraryBook.class, libraryBook.getId());
            CollectionBook expected = CollectionBook.builder()
                    .id(collectionBookId)
                    .collection(collectionRef)
                    .libraryBook(libraryBookRef)
                    .build();
            return repository.save(expected);
        });

        CollectionBook expected = CollectionBook.builder()
                .id(collectionBookId)
                .collection(collection)
                .libraryBook(libraryBook)
                .build();
        assertThat(actual)
                .usingRecursiveComparison(COLLECTION_BOOK_SAVED)
                .isEqualTo(expected);
        CollectionBook dbState = testDbClient.findCollectionBookById(actual.getId().getCollectionId(), actual.getId().getLibraryBookId());
        assertThat(dbState)
                .isNotNull()
                .usingRecursiveComparison(COLLECTION_BOOK_SAVED)
                .isEqualTo(actual);
        assertThat(testDbClient.countCollections()).isEqualTo(initialCollectionCount);
        assertThat(testDbClient.countLibraryBooks()).isEqualTo(initialLibraryBookCount);
    }

    @Test
    @Transactional
    void findById_ShouldReturnCollectionBook_WhenExists() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Collection collection = createCollection(user);
        testDbClient.saveCollection(collection);
        CollectionBookId collectionBookId = new CollectionBookId(collection.getId(), libraryBook.getId());
        CollectionBook cb = CollectionBook.builder()
                .id(collectionBookId)
                .collection(collection)
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveCollectionBook(cb);

        Optional<CollectionBook> actual = repository.findById(collectionBookId);

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(collectionBookId);
        assertThat(actual.get().getCollection())
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(COLLECTION_DIRECT_FIELDS)
                .isEqualTo(collection);
        assertThat(actual.get().getLibraryBook())
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(LIBRARY_BOOK_DIRECT_FIELDS)
                .isEqualTo(libraryBook);
    }

    @Test
    void delete_ShouldRemoveCollectionBook_ButKeepCollectionAndLibraryBook() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Collection collection = createCollection(user);
        testDbClient.saveCollection(collection);
        CollectionBookId collectionBookId = new CollectionBookId(collection.getId(), libraryBook.getId());
        CollectionBook cb = CollectionBook.builder()
                .id(collectionBookId)
                .collection(collection)
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveCollectionBook(cb);
        long initialCollectionCount = testDbClient.countCollections();
        long initialLibraryBookCount = testDbClient.countLibraryBooks();

        repository.deleteById(collectionBookId);

        assertThat(testDbClient.findCollectionBookById(collection.getId(), libraryBook.getId())).isNull();
        assertThat(testDbClient.countCollections()).isEqualTo(initialCollectionCount);
        assertThat(testDbClient.countLibraryBooks()).isEqualTo(initialLibraryBookCount);
    }

    @Test
    void deleteByLibraryBookIdAndUserId_ShouldDeleteMatchingCollectionBook() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Collection collection = createCollection(user);
        testDbClient.saveCollection(collection);
        CollectionBookId collectionBookId = new CollectionBookId(collection.getId(), libraryBook.getId());
        CollectionBook cb = CollectionBook.builder()
                .id(collectionBookId)
                .collection(collection)
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveCollectionBook(cb);

        int actual = transactionTemplate.execute(status -> repository.deleteByLibraryBookIdAndUserId(libraryBook.getId(), user.getId()));

        assertThat(actual).isEqualTo(1);
        assertThat(testDbClient.findCollectionBookById(collection.getId(), libraryBook.getId())).isNull();
    }

    @Test
    void deleteAllByLibraryBookIdInAndUserId_ShouldDeleteMatchingCollectionBooks() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook1 = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook1);
        LibraryBook libraryBook2 = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook2);
        Collection collection = createCollection(user);
        testDbClient.saveCollection(collection);
        CollectionBook cb1 = CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook1.getId()))
                .collection(collection)
                .libraryBook(libraryBook1)
                .build();
        testDbClient.saveCollectionBook(cb1);
        CollectionBook cb2 = CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook2.getId()))
                .collection(collection)
                .libraryBook(libraryBook2)
                .build();
        testDbClient.saveCollectionBook(cb2);
        var libraryBookIds = List.of(libraryBook1.getId(), libraryBook2.getId());

        transactionTemplate.executeWithoutResult(status -> repository.deleteAllByLibraryBookIdInAndUserId(libraryBookIds, user.getId()));

        assertThat(testDbClient.countCollectionBooks()).isZero();
    }

    @Test
    void deleteAllByCollectionIdAndLibraryBookIdInAndUserId_ShouldDeleteMatchingCollectionBooks() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook1 = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook1);
        LibraryBook libraryBook2 = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook2);
        Collection collection = createCollection(user);
        testDbClient.saveCollection(collection);
        CollectionBook cb1 = CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook1.getId()))
                .collection(collection)
                .libraryBook(libraryBook1)
                .build();
        testDbClient.saveCollectionBook(cb1);
        CollectionBook cb2 = CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook2.getId()))
                .collection(collection)
                .libraryBook(libraryBook2)
                .build();
        testDbClient.saveCollectionBook(cb2);

        int actual = transactionTemplate.execute(status -> repository.deleteAllByCollectionIdAndLibraryBookIdInAndUserId(collection.getId(),
                List.of(libraryBook1.getId(), libraryBook2.getId()), user.getId()));

        assertThat(actual).isEqualTo(2);
        assertThat(testDbClient.countCollectionBooks()).isZero();
    }

    @Test
    void deleteByIdAndUserId_ShouldDeleteMatchingCollectionBook() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Collection collection = createCollection(user);
        testDbClient.saveCollection(collection);
        CollectionBook cb = CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook.getId()))
                .collection(collection)
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveCollectionBook(cb);
        var collectionId = collection.getId();
        var bookId = libraryBook.getId();

        int actual = transactionTemplate.execute(status -> repository.deleteByIdAndUserId(collectionId, bookId, user.getId()));

        assertThat(actual).isEqualTo(1);
        assertThat(testDbClient.findCollectionBookById(collection.getId(), libraryBook.getId())).isNull();
    }

    @Test
    void findLibraryBookIdsByCollectionId_ShouldReturnLibraryBookIds() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook1 = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook1);
        LibraryBook libraryBook2 = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook2);
        Collection collection = createCollection(user);
        testDbClient.saveCollection(collection);
        CollectionBook cb1 = CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook1.getId()))
                .collection(collection)
                .libraryBook(libraryBook1)
                .build();
        testDbClient.saveCollectionBook(cb1);
        CollectionBook cb2 = CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook2.getId()))
                .collection(collection)
                .libraryBook(libraryBook2)
                .build();
        testDbClient.saveCollectionBook(cb2);

        Set<Integer> actual = repository.findLibraryBookIdsByCollectionId(collection.getId());

        assertThat(actual).containsExactlyInAnyOrder(libraryBook1.getId(), libraryBook2.getId());
    }

    @Test
    void deleteByLibraryBookIdAndCollectionId_ShouldDeleteMatchingCollectionBook() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Collection collection = createCollection(user);
        testDbClient.saveCollection(collection);
        CollectionBook cb = CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook.getId()))
                .collection(collection)
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveCollectionBook(cb);
        var collectionId = collection.getId();
        var bookId = libraryBook.getId();

        int actual = transactionTemplate.execute(status -> repository.deleteByLibraryBookIdAndCollectionId(bookId, collectionId));

        assertThat(actual).isEqualTo(1);
        assertThat(testDbClient.findCollectionBookById(collection.getId(), libraryBook.getId())).isNull();
    }

    private User createUser(String email) {
        return User.builder()
                .email(email)
                .fullName("Test User")
                .password("password")
                .role(USER)
                .build();
    }

    private Book createBook() {
        Book book = Book.builder()
                .publishYear((short) 2010)
                .pages((short) 400)
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

    private LibraryBook createLibraryBook(Book book, User user) {
        return LibraryBook.builder()
                .book(book)
                .user(user)
                .status(NO_TAG)
                .pages((short) 350)
                .language("English")
                .build();
    }

    private Collection createCollection(User user) {
        return Collection.builder()
                .user(user)
                .name("Favorites")
                .description("Favorite books")
                .build();
    }

}

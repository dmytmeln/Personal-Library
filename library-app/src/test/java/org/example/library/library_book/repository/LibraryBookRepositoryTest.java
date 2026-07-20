package org.example.library.library_book.repository;

import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.book.dto.LanguageWithCount;
import org.example.library.category.domain.Category;
import org.example.library.config.AbstractRepositoryTest;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.dto.BookRatingSummary;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.example.library.config.EntityRecursiveComparisonConfigs.BOOK_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.LIBRARY_BOOK_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.LIBRARY_BOOK_SAVED;
import static org.example.library.config.EntityRecursiveComparisonConfigs.USER_DIRECT_FIELDS;
import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;
import static org.example.library.user.domain.Role.USER;

class LibraryBookRepositoryTest extends AbstractRepositoryTest<LibraryBookRepository> {

    @Test
    void save_ShouldPersistLibraryBook_AndNotCascadeBookOrUser() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        long initialUserCount = testDbClient.countUsers();
        long initialBookCount = testDbClient.countBooks();
        LibraryBook expected = createLibraryBook(book, user);

        LibraryBook actual = repository.save(expected);

        assertThat(actual)
                .usingRecursiveComparison(LIBRARY_BOOK_SAVED)
                .isEqualTo(expected);
        LibraryBook dbState = testDbClient.findLibraryBookById(actual.getId());
        assertThat(dbState)
                .isNotNull()
                .usingRecursiveComparison(LIBRARY_BOOK_DIRECT_FIELDS)
                .isEqualTo(actual);
        assertThat(testDbClient.countUsers()).isEqualTo(initialUserCount);
        assertThat(testDbClient.countBooks()).isEqualTo(initialBookCount);
    }

    @Test
    @Transactional
    void findById_ShouldReturnLibraryBook_WhenExists() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(lb);

        Optional<LibraryBook> actual = repository.findById(lb.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(LIBRARY_BOOK_DIRECT_FIELDS)
                .isEqualTo(lb);
        assertThat(actual.get().getBook())
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(BOOK_DIRECT_FIELDS)
                .isEqualTo(book);
        assertThat(actual.get().getUser())
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(USER_DIRECT_FIELDS)
                .isEqualTo(user);
    }

    @Test
    void delete_ShouldRemoveLibraryBook_ButKeepBookAndUser() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(lb);
        long initialUserCount = testDbClient.countUsers();
        long initialBookCount = testDbClient.countBooks();

        repository.deleteById(lb.getId());

        assertThat(testDbClient.findLibraryBookById(lb.getId())).isNull();
        assertThat(testDbClient.countUsers()).isEqualTo(initialUserCount);
        assertThat(testDbClient.countBooks()).isEqualTo(initialBookCount);
    }

    @Test
    void findLanguagesWithCountByUserId_ShouldReturnLanguageCounts() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book1 = createBook(null, null);
        testDbClient.saveBook(book1);
        LibraryBook lb1 = createLibraryBook(book1, user);
        lb1.setLanguage("Spanish");
        testDbClient.saveLibraryBook(lb1);
        Book book2 = createBook(null, null);
        testDbClient.saveBook(book2);
        LibraryBook lb2 = createLibraryBook(book2, user);
        lb2.setLanguage("Spanish");
        testDbClient.saveLibraryBook(lb2);
        Book book3 = createBook(null, null);
        testDbClient.saveBook(book3);
        LibraryBook lb3 = createLibraryBook(book3, user);
        lb3.setLanguage("French");
        testDbClient.saveLibraryBook(lb3);
        Book book4 = createBook(null, null);
        testDbClient.saveBook(book4);
        LibraryBook lb4 = createLibraryBook(book4, user);
        lb4.setLanguage(null);
        testDbClient.saveLibraryBook(lb4);

        List<LanguageWithCount> actual = repository.findLanguagesWithCountByUserId(user.getId(), "en");

        assertThat(actual).hasSize(3);
        assertThat(actual.get(0).getLanguage()).isEqualTo("Spanish");
        assertThat(actual.get(0).getCount()).isEqualTo(2L);
        assertThat(actual.get(1).getLanguage()).isEqualTo("English");
        assertThat(actual.get(1).getCount()).isEqualTo(1L);
        assertThat(actual.get(2).getLanguage()).isEqualTo("French");
        assertThat(actual.get(2).getCount()).isEqualTo(1L);
    }

    @Test
    void findAverageRatingAndCountByBookId_ShouldReturnAverageRatingAndCount() {
        User user1 = createUser("user1@example.com");
        testDbClient.saveUser(user1);
        User user2 = createUser("user2@example.com");
        testDbClient.saveUser(user2);
        User user3 = createUser("user3@example.com");
        testDbClient.saveUser(user3);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb1 = createLibraryBook(book, user1);
        lb1.setRating((byte) 4);
        testDbClient.saveLibraryBook(lb1);
        LibraryBook lb2 = createLibraryBook(book, user2);
        lb2.setRating((byte) 5);
        testDbClient.saveLibraryBook(lb2);
        LibraryBook lb3 = createLibraryBook(book, user3);
        lb3.setRating(null);
        testDbClient.saveLibraryBook(lb3);

        BookRatingSummary actual = repository.findAverageRatingAndCountByBookId(book.getId());

        assertThat(actual.getAverageRating()).isEqualTo(4.5);
        assertThat(actual.getRatingsCount()).isEqualTo(2L);
    }

    @Test
    void findExistingBookIdsInLibrary_ShouldReturnExistingBookIds() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book1 = createBook(null, null);
        testDbClient.saveBook(book1);
        Book book2 = createBook(null, null);
        testDbClient.saveBook(book2);
        Book book3 = createBook(null, null);
        testDbClient.saveBook(book3);
        LibraryBook lb1 = createLibraryBook(book1, user);
        testDbClient.saveLibraryBook(lb1);
        LibraryBook lb2 = createLibraryBook(book2, user);
        testDbClient.saveLibraryBook(lb2);

        List<Integer> actual = repository.findExistingBookIdsInLibrary(user.getId(), List.of(book1.getId(), book2.getId(), book3.getId()));

        assertThat(actual).containsExactlyInAnyOrder(book1.getId(), book2.getId());
    }

    @Test
    void updateRating_ShouldUpdateRating() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb = createLibraryBook(book, user);
        lb.setRating((byte) 3);
        testDbClient.saveLibraryBook(lb);

        Integer actual = transactionTemplate.execute(status -> repository.updateRating(lb.getId(), user.getId(), (byte) 5));

        LibraryBook dbState = testDbClient.findLibraryBookById(lb.getId());
        assertThat(actual).isEqualTo(1);
        assertThat(dbState.getRating()).isEqualTo((byte) 5);
    }

    @Test
    void resetOverriddenFields_ShouldResetFieldsToNull() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb = createLibraryBook(book, user);
        lb.setTitle("Overridden Title");
        lb.setPublishYear((short) 2020);
        lb.setPages((short) 500);
        lb.setLanguage("German");
        lb.setDescription("Overridden description");
        testDbClient.saveLibraryBook(lb);

        Integer actual = transactionTemplate.execute(status -> repository.resetOverriddenFields(lb.getId(), user.getId()));

        LibraryBook dbState = testDbClient.findLibraryBookById(lb.getId());
        assertThat(actual).isEqualTo(1);
        assertThat(dbState.getTitle()).isNull();
        assertThat(dbState.getPublishYear()).isNull();
        assertThat(dbState.getPages()).isNull();
        assertThat(dbState.getLanguage()).isNull();
        assertThat(dbState.getDescription()).isNull();
    }

    @Test
    void findByIdAndUserId_ShouldReturnLibraryBook() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(lb);

        Optional<LibraryBook> actual = repository.findByIdAndUserId(lb.getId(), user.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(lb.getId());
    }

    @Test
    void findByIdAndUserIdWithBook_ShouldReturnLibraryBookWithBook() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(lb);

        Optional<LibraryBook> actual = repository.findByIdAndUserIdWithBook(lb.getId(), user.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    void existsByIdAndUserId_ShouldReturnTrue_WhenExists() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(lb);

        boolean actual = repository.existsByIdAndUserId(lb.getId(), user.getId());

        assertThat(actual).isTrue();
    }

    @Test
    void existsByIdAndUserId_ShouldReturnFalse_WhenDoesNotExist() {
        boolean actual = repository.existsByIdAndUserId(999, 999);

        assertThat(actual).isFalse();
    }

    @Test
    void existsByBookIdAndUserId_ShouldReturnTrue_WhenExists() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(lb);

        boolean actual = repository.existsByBookIdAndUserId(book.getId(), user.getId());

        assertThat(actual).isTrue();
    }

    @Test
    void existsByBookIdAndUserId_ShouldReturnFalse_WhenDoesNotExist() {
        boolean actual = repository.existsByBookIdAndUserId(999, 999);

        assertThat(actual).isFalse();
    }

    @Test
    void findAllWithVectorsByUserId_ShouldReturnLibraryBooksWithBookEmbedding() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book1 = createBook(null, null);
        testDbClient.saveBook(book1);
        LibraryBook lb1 = createLibraryBook(book1, user);
        testDbClient.saveLibraryBook(lb1);
        Book book2 = createBook(null, null);
        book2.setEmbedding(null);
        testDbClient.saveBook(book2);
        LibraryBook lb2 = createLibraryBook(book2, user);
        testDbClient.saveLibraryBook(lb2);

        List<LibraryBook> actual = repository.findAllWithVectorsByUserId(user.getId());

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getId()).isEqualTo(lb1.getId());
    }

    @Test
    void findAllByIdInAndUserId_ShouldReturnMatchingLibraryBooks() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb1 = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(lb1);
        LibraryBook lb2 = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(lb2);

        List<LibraryBook> actual = repository.findAllByIdInAndUserId(List.of(lb1.getId(), lb2.getId()), user.getId());

        assertThat(actual).hasSize(2);
    }

    @Test
    void findAllByIdInAndUserIdWithBook_ShouldReturnMatchingLibraryBooksWithBook() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(lb);

        List<LibraryBook> actual = repository.findAllByIdInAndUserIdWithBook(List.of(lb.getId()), user.getId());

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    void findAllByUserIdWithBook_ShouldReturnAllWithBook() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(lb);

        List<LibraryBook> actual = repository.findAllByUserIdWithBook(user.getId());

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    void findAllByUserId_ShouldReturnAll() {
        User user = createUser("test@example.com");
        testDbClient.saveUser(user);
        Book book = createBook(null, null);
        testDbClient.saveBook(book);
        LibraryBook lb = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(lb);

        List<LibraryBook> actual = repository.findAllByUserId(user.getId());

        assertThat(actual).hasSize(1);
    }

    private User createUser(String email) {
        return User.builder()
                .email(email)
                .fullName("Test User")
                .password("password")
                .role(USER)
                .build();
    }

    private Book createBook(User owner, Category category) {
        Book book = Book.builder()
                .owner(owner)
                .category(category)
                .publishYear((short) 2010)
                .pages((short) 400)
                .coverImageUrl("http://example.com/cover.png")
                .embedding(createEmbedding())
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

    private float[] createEmbedding() {
        float[] embedding = new float[384];
        embedding[0] = 0.1f;
        embedding[1] = 0.2f;
        embedding[2] = 0.3f;

        return embedding;
    }

    private LibraryBook createLibraryBook(Book book, User user) {
        return LibraryBook.builder()
                .book(book)
                .user(user)
                .status(NO_TAG)
                .finishedAt(LocalDate.of(2023, 5, 10))
                .rating((byte) 4)
                .title("Custom Title")
                .publishYear((short) 2021)
                .pages((short) 350)
                .language("English")
                .description("Custom Description")
                .location("Shelf A")
                .customAuthorName("Custom Author")
                .customCategoryName("Custom Category")
                .build();
    }

}

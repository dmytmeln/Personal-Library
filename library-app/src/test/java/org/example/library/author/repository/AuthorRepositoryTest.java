package org.example.library.author.repository;

import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorDisplayView;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.author.dto.AuthorSearchParams;
import org.example.library.author.dto.AuthorWithBooksCount;
import org.example.library.author.dto.CountryWithCount;
import org.example.library.book.domain.Book;
import org.example.library.config.AbstractRepositoryTest;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.example.library.config.EntityRecursiveComparisonConfigs.AUTHOR_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.AUTHOR_SAVED;
import static org.example.library.config.EntityRecursiveComparisonConfigs.AUTHOR_TRANSLATION_SAVED;
import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;
import static org.example.library.user.domain.Role.USER;

class AuthorRepositoryTest extends AbstractRepositoryTest<AuthorRepository> {

    @Test
    void save_ShouldPersistAuthor_AndCascadeTranslations() {
        Author expected = createAuthor();

        Author actual = repository.save(expected);

        assertThat(actual)
                .usingRecursiveComparison(AUTHOR_SAVED)
                .isEqualTo(expected);
        Author dbState = testDbClient.findAuthorById(actual.getId());
        assertThat(dbState)
                .isNotNull()
                .usingRecursiveComparison(AUTHOR_DIRECT_FIELDS)
                .isEqualTo(actual);
        assertThat(dbState.getTranslations()).hasSize(1);
        AuthorTranslation actualTranslation = actual.getTranslations().get("en");
        AuthorTranslation dbTranslation = dbState.getTranslations().get("en");
        assertThat(dbTranslation)
                .isNotNull()
                .usingRecursiveComparison(AUTHOR_TRANSLATION_SAVED)
                .isEqualTo(actualTranslation);
    }

    @Test
    @Transactional
    void findById_ShouldReturnAuthor_WhenAuthorExists() {
        Author author = createAuthor();
        testDbClient.saveAuthor(author);

        Optional<Author> actual = repository.findById(author.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(AUTHOR_DIRECT_FIELDS)
                .isEqualTo(author);
        assertThat(actual.get().getTranslations()).hasSize(1);
        assertThat(actual.get().getTranslations().get("en"))
                .usingRecursiveComparison(AUTHOR_TRANSLATION_SAVED)
                .isEqualTo(author.getTranslations().get("en"));
    }

    @Test
    void findDisplayViewByIdAndLanguageCode_ShouldReturnAuthorDisplayView_WhenAuthorAndTranslationExist() {
        Author author = createAuthor();
        testDbClient.saveAuthor(author);

        Optional<AuthorDisplayView> actual = repository.findDisplayViewByIdAndLanguageCode(author.getId(), "en");

        assertThat(actual).isPresent();
        AuthorDisplayView view = actual.get();
        assertThat(view.getId()).isEqualTo(author.getId());
        assertThat(view.getBirthYear()).isEqualTo((short) 1900);
        assertThat(view.getDeathYear()).isEqualTo((short) 1980);
        assertThat(view.getPopularityCount()).isZero();
        assertThat(view.getLanguageCode()).isEqualTo("en");
        assertThat(view.getFullName()).isEqualTo("John Doe");
        assertThat(view.getCountry()).isEqualTo("USA");
        assertThat(view.getBiography()).isEqualTo("Bio info");
    }

    @Test
    void searchWithBooksCount_ShouldReturnPaginatedAuthorsWithBooksCount() {
        Author author = createAuthor();
        testDbClient.saveAuthor(author);
        Book book = createBook();
        testDbClient.saveBook(book);
        testDbClient.linkBookToAuthor(book.getId(), author.getId());

        AuthorSearchParams searchParams = new AuthorSearchParams();
        searchParams.setName("John");

        Page<AuthorWithBooksCount> actual = repository.searchWithBooksCount(
                searchParams, "en", PageRequest.of(0, 10));

        assertThat(actual.getContent()).hasSize(1);
        AuthorWithBooksCount dto = actual.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(author.getId());
        assertThat(dto.getFullName()).isEqualTo("John Doe");
        assertThat(dto.getBooksCount()).isEqualTo(1L);
    }

    @Test
    void findAllCountriesWithCount_ShouldReturnCountryCounts() {
        Author author1 = createAuthor();
        testDbClient.saveAuthor(author1);
        Author author2 = createAuthor();
        author2.getTranslations().get("en").setCountry("UK");
        author2.getTranslations().get("en").setFullName("Jane Doe");
        testDbClient.saveAuthor(author2);

        List<CountryWithCount> actual = repository.findAllCountriesWithCount("en");

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getCountry()).isEqualTo("UK");
        assertThat(actual.get(0).getCount()).isEqualTo(1L);
        assertThat(actual.get(1).getCountry()).isEqualTo("USA");
        assertThat(actual.get(1).getCount()).isEqualTo(1L);
    }

    @Test
    void searchForUser_ShouldReturnAuthorsWithBooksCountForSpecificUser() {
        User user = User.builder()
                .email("user-author@example.com")
                .fullName("User Author")
                .password("password")
                .role(USER)
                .build();
        testDbClient.saveUser(user);
        Author author = createAuthor();
        testDbClient.saveAuthor(author);
        Book book = createBook();
        testDbClient.saveBook(book);
        testDbClient.linkBookToAuthor(book.getId(), author.getId());
        LibraryBook libraryBook = LibraryBook.builder()
                .book(book)
                .user(user)
                .status(NO_TAG)
                .build();
        testDbClient.saveLibraryBook(libraryBook);

        AuthorSearchParams searchParams = new AuthorSearchParams();
        searchParams.setName("John");

        Page<AuthorWithBooksCount> actual = repository.searchForUser(user.getId(),
                searchParams,
                "en",
                PageRequest.of(0, 10));

        assertThat(actual.getContent()).hasSize(1);
        AuthorWithBooksCount dto = actual.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(author.getId());
        assertThat(dto.getBooksCount()).isEqualTo(1L);
    }

    @Test
    void findAllCountriesForUser_ShouldReturnCountryCountsForSpecificUser() {
        User user = User.builder()
                .email("user-countries@example.com")
                .fullName("User Countries")
                .password("password")
                .role(USER)
                .build();
        testDbClient.saveUser(user);
        Author author1 = createAuthor();
        testDbClient.saveAuthor(author1);
        Author author2 = createAuthor();
        author2.getTranslations().get("en").setCountry("UK");
        author2.getTranslations().get("en").setFullName("Jane Doe");
        testDbClient.saveAuthor(author2);
        Book book1 = createBook();
        testDbClient.saveBook(book1);
        testDbClient.linkBookToAuthor(book1.getId(), author1.getId());
        Book book2 = createBook();
        testDbClient.saveBook(book2);
        testDbClient.linkBookToAuthor(book2.getId(), author2.getId());
        LibraryBook lb1 = LibraryBook.builder()
                .book(book1)
                .user(user)
                .status(NO_TAG)
                .build();
        testDbClient.saveLibraryBook(lb1);
        LibraryBook lb2 = LibraryBook.builder()
                .book(book2)
                .user(user)
                .status(NO_TAG)
                .build();
        testDbClient.saveLibraryBook(lb2);

        List<CountryWithCount> actual = repository.findAllCountriesForUser(user.getId(), "en");

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getCountry()).isEqualTo("UK");
        assertThat(actual.get(0).getCount()).isEqualTo(1L);
        assertThat(actual.get(1).getCountry()).isEqualTo("USA");
        assertThat(actual.get(1).getCount()).isEqualTo(1L);
    }

    @Test
    void incrementPopularityCountByBookIds_ShouldIncrementCount() {
        Author author = createAuthor();
        testDbClient.saveAuthor(author);
        Book book = createBook();
        testDbClient.saveBook(book);
        testDbClient.linkBookToAuthor(book.getId(), author.getId());

        transactionTemplate.executeWithoutResult(status -> repository.incrementPopularityCountByBookIds(List.of(book.getId())));

        Author dbState = testDbClient.findAuthorById(author.getId());
        assertThat(dbState.getPopularityCount()).isEqualTo(1);
    }

    @Test
    void decrementPopularityCountByBookIds_ShouldDecrementCount() {
        Author author = createAuthor();
        author.setPopularityCount(5);
        testDbClient.saveAuthor(author);
        Book book = createBook();
        testDbClient.saveBook(book);
        testDbClient.linkBookToAuthor(book.getId(), author.getId());

        transactionTemplate.executeWithoutResult(status -> repository.decrementPopularityCountByBookIds(List.of(book.getId())));

        Author dbState = testDbClient.findAuthorById(author.getId());
        assertThat(dbState.getPopularityCount()).isEqualTo(4);
    }

    @Test
    void delete_ShouldRemoveAuthor_AndCascadeTranslations() {
        Author author = createAuthor();
        testDbClient.saveAuthor(author);

        repository.deleteById(author.getId());

        assertThat(testDbClient.findAuthorById(author.getId())).isNull();
        assertThat(testDbClient.countAuthorTranslations()).isZero();
    }

    private Author createAuthor() {
        Author author = Author.builder()
                .birthYear((short) 1900)
                .deathYear((short) 1980)
                .popularityCount(0)
                .build();

        AuthorTranslation translation = AuthorTranslation.builder()
                .languageCode("en")
                .fullName("John Doe")
                .country("USA")
                .biography("Bio info")
                .author(author)
                .build();
        author.getTranslations().put("en", translation);

        return author;
    }

    private Book createBook() {
        return Book.builder()
                .publishYear((short) 1950)
                .pages((short) 300)
                .coverImageUrl("http://example.com/cover.png")
                .status(NEW)
                .popularityCount(0)
                .build();
    }

}

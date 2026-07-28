package org.example.library.author.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.author.dto.AuthorSaveRequest;
import org.example.library.author.dto.AuthorSaveRequest.AuthorTranslationRequest;
import org.example.library.author.dto.AuthorSearchParams;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookStatus;
import org.example.library.book.domain.BookTranslation;
import org.example.library.book.repository.BookRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.example.library.common.pagination.PaginationParams;
import org.example.library.config.PostgresTestContainer;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.repository.LibraryBookRepository;
import org.example.library.user.domain.User;
import org.example.library.user.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // todo
@ContextConfiguration(initializers = PostgresTestContainer.class)
class AuthorServiceIntegrationTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private AuthorRepository repository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LibraryBookRepository libraryBookRepository;

    @Autowired
    private AuthorService service;

    @BeforeAll
    static void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void cleanUpLocaleEach() {
        LocaleContextHolder.resetLocaleContext();
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterAll
    static void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void shouldReturnAuthorWhenGetById() {
        var expected = saveAuthor("John Doe", "USA");
        em.flush();
        em.clear();

        var result = service.getLocalizedAuthor(expected.getId());

        assertThat(result.fullName()).isEqualTo("John Doe");
        assertThat(result.country()).isEqualTo("USA");
    }

    @Test
    void shouldThrowNotFoundWhenGetByIdWithoutExistingAuthor() {
        assertThatThrownBy(() -> service.getLocalizedAuthor(-99999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.author.not_found");
    }

    @Test
    void shouldSearchAuthorsWithBooksCount() {
        var author = saveAuthor("Author 1", "UK");
        saveBook(author);
        saveBook(author);
        var otherAuthor = saveAuthor("Author 2", "USA");
        saveBook(otherAuthor);
        em.flush();
        em.clear();
        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setSort(List.of("fullName;asc"));
        var searchParams = new AuthorSearchParams();
        searchParams.setName("Author 1");

        var result = service.searchInCatalog(pagination, searchParams);

        assertThat(result.getContent())
                .hasSize(2)
                .extracting("fullName")
                .contains("Author 1", "Author 2");
    }

    @Test
    void shouldSearchAuthorsByBooksCountRange() {
        var auth1 = saveAuthor("Auth 1", "Country");
        saveBook(auth1);
        var auth2 = saveAuthor("Auth 2", "Country");
        saveBook(auth2);
        saveBook(auth2);
        var auth3 = saveAuthor("Auth 3", "Country");
        saveBook(auth3);
        saveBook(auth3);
        saveBook(auth3);
        em.flush();
        em.clear();
        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setSort(List.of("fullName;asc"));
        var searchParams = new AuthorSearchParams();
        searchParams.setBooksCountMin(2);
        searchParams.setBooksCountMax(2);

        var result = service.searchInCatalog(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("Auth 2");
        assertThat(result.getContent().get(0).getBooksCount()).isEqualTo(2);
    }

    @Test
    void shouldFindAuthorWithTypo() {
        saveAuthor("John Doe", "USA");
        em.flush();
        em.clear();
        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new AuthorSearchParams();
        searchParams.setName("John Doee");

        var result = service.searchInCatalog(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("John Doe");
    }

    @Test
    void shouldSearchAuthorsForUser() {
        var user = User.builder().email("test@example.com").fullName("Test User").password("pass").build();
        userRepository.save(user);
        var author = saveAuthor("User Author", "USA");
        var book1 = saveBook(author);
        var book2 = saveBook(author);
        libraryBookRepository.save(LibraryBook.builder().user(user).book(book1).title("Title 1").build());
        libraryBookRepository.save(LibraryBook.builder().user(user).book(book2).title("Title 2").build());
        em.flush();
        em.clear();
        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setSort(List.of("fullName;asc"));
        var searchParams = new AuthorSearchParams();

        var result = service.searchInUserLibrary(user.getId(), pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("User Author");
        assertThat(result.getContent().get(0).getBooksCount()).isEqualTo(2);
    }

    @Test
    void shouldReturnAllCountries() {
        saveAuthor("Auth 1", "USA");
        saveAuthor("Auth 2", "UK");
        saveAuthor("Auth 3", "USA");
        em.flush();
        em.clear();

        var countries = service.getAuthorCountriesWithCount();

        assertThat(countries).hasSize(2);
        assertThat(countries).extracting("country").containsExactlyInAnyOrder("USA", "UK");
    }

    @Test
    void shouldReturnCountriesForUser() {
        var user = User.builder().email("user@example.com").fullName("User").password("pass").build();
        userRepository.save(user);
        var auth1 = saveAuthor("Auth 1", "USA");
        var auth2 = saveAuthor("Auth 2", "UK");
        var book1 = saveBook(auth1);
        var book2 = saveBook(auth2);
        libraryBookRepository.save(LibraryBook.builder().user(user).book(book1).title("T1").build());
        libraryBookRepository.save(LibraryBook.builder().user(user).book(book2).title("T2").build());
        em.flush();
        em.clear();

        var countries = service.getUserAuthorCountriesWithCount(user.getId());

        assertThat(countries).hasSize(2);
        assertThat(countries).extracting("country").containsExactlyInAnyOrder("USA", "UK");
    }

    @Test
    void shouldReturnAuthorResponseWhenGetAuthor() {
        var author = saveAuthor("Author Name", "USA");
        em.flush();
        em.clear();

        var result = service.getAuthorWithAllTranslations(author.getId());

        assertThat(result.getId()).isEqualTo(author.getId());
        assertThat(result.getTranslations().get("en").getFullName()).isEqualTo("Author Name");
    }

    @Test
    void shouldThrowNotFoundWhenGetAuthorByIdWithoutExistingAuthor() {
        assertThatThrownBy(() -> service.getAuthorWithAllTranslations(-1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.author.not_found");
    }

    @Test
    void shouldCreateAuthor() {
        var dto = AuthorSaveRequest.builder()
                .birthYear((short) 1950)
                .translations(Map.of("en", AuthorTranslationRequest.builder()
                        .fullName("New Author")
                        .country("USA")
                        .biography("Bio")
                        .build()))
                .build();

        var response = service.saveAuthor(dto);
        em.flush();
        em.clear();

        assertThat(response).isNotNull();
        assertThat(response.getTranslations().get("en").getFullName()).isEqualTo("New Author");

        var authors = repository.findAll();
        assertThat(authors).hasSize(1);
        var author = authors.get(0);
        assertThat(author.getTranslations().get("en").getFullName()).isEqualTo("New Author");
        assertThat(author.getBirthYear()).isEqualTo((short) 1950);
    }

    @Test
    void shouldThrowBadRequestWhenCreateAuthorWithoutDefaultLanguageTranslation() {
        var dto = AuthorSaveRequest.builder()
                .birthYear((short) 1950)
                .translations(Map.of("fr", AuthorTranslationRequest.builder()
                        .fullName("Auteur")
                        .country("France")
                        .build()))
                .build();

        assertThatThrownBy(() -> service.saveAuthor(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.author.default_translation_missing");
    }

    @Test
    void shouldUpdateAuthor() {
        var author = saveAuthor("Old Name", "USA");
        var dto = AuthorSaveRequest.builder()
                .birthYear(author.getBirthYear())
                .deathYear((short) 2000)
                .translations(Map.of("en", AuthorTranslationRequest.builder()
                        .fullName("New Name")
                        .country("USA")
                        .build()))
                .build();
        em.flush();
        em.clear();

        var response = service.updateAuthor(author.getId(), dto);
        em.flush();
        em.clear();

        assertThat(response.getTranslations().get("en").getFullName()).isEqualTo("New Name");
        assertThat(response.getDeathYear()).isEqualTo((short) 2000);

        var updatedAuthor = repository.findById(author.getId())
                .orElseThrow(() -> new AssertionError("Author not found after update"));
        assertThat(updatedAuthor.getTranslations().get("en").getFullName()).isEqualTo("New Name");
        assertThat(updatedAuthor.getDeathYear()).isEqualTo((short) 2000);
    }

    @Test
    void shouldRemoveOmittedTranslationsOnUpdate() {
        var author = saveAuthor("John Doe", "USA");
        author.upsertTranslation("uk", "Джон Доу", "США", "Біографія");
        repository.save(author);
        em.flush();
        em.clear();
        var dto = AuthorSaveRequest.builder()
                .birthYear(author.getBirthYear())
                .translations(Map.of("en", AuthorTranslationRequest.builder()
                        .fullName("John Doe")
                        .country("USA")
                        .biography("Biography")
                        .build()))
                .build();

        service.updateAuthor(author.getId(), dto);
        em.flush();
        em.clear();

        var updatedAuthor = repository.findById(author.getId())
                .orElseThrow(() -> new AssertionError("Author not found after update"));
        assertThat(updatedAuthor.getTranslations()).containsKey("en");
        assertThat(updatedAuthor.getTranslations()).doesNotContainKey("uk");
    }

    @Test
    void shouldDeleteAuthor() {
        var author = saveAuthor("Author", "USA");
        em.flush();
        em.clear();

        service.deleteAuthor(author.getId());
        em.flush();
        em.clear();

        assertThat(repository.existsById(author.getId())).isFalse();
    }

    @Test
    void shouldThrowBadRequestWhenDeleteAuthorWithBooks() {
        var author = saveAuthor("Author", "USA");
        saveBook(Set.of(author));
        em.flush();
        em.clear();

        var authorId = author.getId();
        assertThatThrownBy(() -> service.deleteAuthor(authorId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.author.has_books");
    }

    @Test
    void shouldDeleteAuthorsBulk() {
        var a1 = saveAuthor("A1", "USA");
        var a2 = saveAuthor("A2", "USA");
        em.flush();
        em.clear();

        service.deleteAuthors(List.of(a1.getId(), a2.getId()));
        em.flush();
        em.clear();

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldThrowBadRequestWhenDeleteAuthorsBulkWithBooks() {
        var a1 = saveAuthor("A1", "USA");
        var a2 = saveAuthor("A2", "USA");
        saveBook(Set.of(a1));
        em.flush();
        em.clear();

        var authorIds = List.of(a1.getId(), a2.getId());
        assertThatThrownBy(() -> service.deleteAuthors(authorIds))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.author.has_books");
    }

    private Author saveAuthor(String fullName, String country) {
        var author = Author.builder()
                .birthYear((short) 1900)
                .popularityCount(0)
                .build();

        var translation = AuthorTranslation.builder()
                .languageCode("en")
                .fullName(fullName)
                .country(country)
                .biography("Biography of " + fullName)
                .author(author)
                .build();
        author.setTranslations(new HashMap<>(Map.of("en", translation)));

        return repository.save(author);
    }

    private Book saveBook(Author author) {
        return saveBook(Set.of(author));
    }

    private Book saveBook(Set<Author> authors) {
        var book = Book.builder()
                .authors(authors)
                .status(BookStatus.PRELIMINARY)
                .popularityCount(0)
                .build();

        var translation = BookTranslation.builder()
                .languageCode("en")
                .title("Book")
                .bookLanguage("English")
                .description("Desc Book")
                .book(book)
                .build();
        book.setTranslations(new HashMap<>(Map.of("en", translation)));

        return bookRepository.save(book);
    }

}

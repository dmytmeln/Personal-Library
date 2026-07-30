package org.example.library.author.service;

import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.author.dto.AuthorSaveRequest;
import org.example.library.author.dto.AuthorSaveRequest.AuthorTranslationRequest;
import org.example.library.author.dto.AuthorSearchParams;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.example.library.common.pagination.PaginationParams;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private AuthorService service;

    @Test
    void shouldReturnAuthorWhenGetById() {
        var expected = saveAuthor(a -> a.fullName("John Doe").country("USA"));

        var result = service.getLocalizedAuthor(expected.getId());

        assertThat(result.fullName()).isEqualTo("John Doe");
        assertThat(result.country()).isEqualTo("USA");
    }

    @Test
    void shouldThrowNotFoundWhenGetByIdWithoutExistingAuthor() {
        var nonExistingId = -99999;

        assertThatThrownBy(() -> service.getLocalizedAuthor(nonExistingId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.author.not_found");
    }

    @Test
    void shouldSearchAuthorsWithBooksCount() {
        var author = saveAuthor(a -> a.fullName("Author 1").country("UK"));
        saveBook(b -> b.title("Book").bookLanguage("English").authors(author));
        saveBook(b -> b.title("Book").bookLanguage("English").authors(author));
        var otherAuthor = saveAuthor(a -> a.fullName("Author 2").country("USA"));
        saveBook(b -> b.title("Book").bookLanguage("English").authors(otherAuthor));
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
        var auth1 = saveAuthor(a -> a.fullName("Auth 1").country("Country"));
        saveBook(b -> b.title("Book").bookLanguage("English").authors(auth1));
        var auth2 = saveAuthor(a -> a.fullName("Auth 2").country("Country"));
        saveBook(b -> b.title("Book").bookLanguage("English").authors(auth2));
        saveBook(b -> b.title("Book").bookLanguage("English").authors(auth2));
        var auth3 = saveAuthor(a -> a.fullName("Auth 3").country("Country"));
        saveBook(b -> b.title("Book").bookLanguage("English").authors(auth3));
        saveBook(b -> b.title("Book").bookLanguage("English").authors(auth3));
        saveBook(b -> b.title("Book").bookLanguage("English").authors(auth3));
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
        saveAuthor(a -> a.fullName("John Doe").country("USA"));
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
        var user = saveUser(u -> u.email("test@example.com"));
        var author = saveAuthor(a -> a.fullName("User Author").country("USA"));
        var book1 = saveBook(b -> b.title("Book").bookLanguage("English").authors(author));
        var book2 = saveBook(b -> b.title("Book").bookLanguage("English").authors(author));
        saveLibraryBook(lb -> lb.user(user).book(book1));
        saveLibraryBook(lb -> lb.user(user).book(book2));
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
        saveAuthor(a -> a.fullName("Auth 1").country("USA"));
        saveAuthor(a -> a.fullName("Auth 2").country("UK"));
        saveAuthor(a -> a.fullName("Auth 3").country("USA"));

        var countries = service.getAuthorCountriesWithCount();

        assertThat(countries).hasSize(2);
        assertThat(countries).extracting("country").containsExactlyInAnyOrder("USA", "UK");
    }

    @Test
    void shouldReturnCountriesForUser() {
        var user = saveUser(u -> u.email("user@example.com").fullName("User"));
        var auth1 = saveAuthor(a -> a.fullName("Auth 1").country("USA"));
        var auth2 = saveAuthor(a -> a.fullName("Auth 2").country("UK"));
        var book1 = saveBook(b -> b.title("Book").bookLanguage("English").authors(auth1));
        var book2 = saveBook(b -> b.title("Book").bookLanguage("English").authors(auth2));
        saveLibraryBook(lb -> lb.user(user).book(book1));
        saveLibraryBook(lb -> lb.user(user).book(book2));

        var countries = service.getUserAuthorCountriesWithCount(user.getId());

        assertThat(countries).hasSize(2);
        assertThat(countries).extracting("country").containsExactlyInAnyOrder("USA", "UK");
    }

    @Test
    void shouldReturnAuthorResponseWhenGetAuthor() {
        var author = saveAuthor(a -> a.fullName("Author Name").country("USA"));

        var result = service.getAuthorWithAllTranslations(author.getId());

        assertThat(result.getId()).isEqualTo(author.getId());
        assertThat(result.getTranslations().get("en").getFullName()).isEqualTo("Author Name");
    }

    @Test
    void shouldThrowNotFoundWhenGetAuthorByIdWithoutExistingAuthor() {
        var nonExistingId = -1;

        assertThatThrownBy(() -> service.getAuthorWithAllTranslations(nonExistingId))
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

        assertThat(response).isNotNull();
        assertThat(response.getTranslations().get("en").getFullName()).isEqualTo("New Author");

        var author = testDbClient.findAuthorById(response.getId());
        assertThat(author).isNotNull();
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
        var author = saveAuthor(a -> a.fullName("Old Name").country("USA"));
        var dto = AuthorSaveRequest.builder()
                .birthYear(author.getBirthYear())
                .deathYear((short) 2000)
                .translations(Map.of("en", AuthorTranslationRequest.builder()
                        .fullName("New Name")
                        .country("USA")
                        .build()))
                .build();

        var response = service.updateAuthor(author.getId(), dto);

        assertThat(response.getTranslations().get("en").getFullName()).isEqualTo("New Name");
        assertThat(response.getDeathYear()).isEqualTo((short) 2000);

        var updatedAuthor = testDbClient.findAuthorById(author.getId());
        assertThat(updatedAuthor).isNotNull();
        assertThat(updatedAuthor.getTranslations().get("en").getFullName()).isEqualTo("New Name");
        assertThat(updatedAuthor.getDeathYear()).isEqualTo((short) 2000);
    }

    @Test
    void shouldRemoveOmittedTranslationsOnUpdate() {
        var ukTranslation = AuthorTranslation.builder()
                .languageCode("uk")
                .fullName("Джон Доу")
                .country("США")
                .biography("Біографія")
                .build();
        var author = Author.builder()
                .birthYear((short) 1900)
                .build();
        var enTranslation = AuthorTranslation.builder()
                .languageCode("en")
                .fullName("John Doe")
                .country("USA")
                .biography("Biography of John Doe")
                .author(author)
                .build();
        ukTranslation.setAuthor(author);
        author.setTranslations(new HashMap<>(Map.of("en", enTranslation, "uk", ukTranslation)));
        testDbClient.saveAuthor(author);
        var dto = AuthorSaveRequest.builder()
                .birthYear(author.getBirthYear())
                .translations(Map.of("en", AuthorTranslationRequest.builder()
                        .fullName("John Doe")
                        .country("USA")
                        .biography("Biography")
                        .build()))
                .build();

        service.updateAuthor(author.getId(), dto);

        var updatedAuthor = testDbClient.findAuthorById(author.getId());
        assertThat(updatedAuthor).isNotNull();
        assertThat(updatedAuthor.getTranslations()).containsKey("en");
        assertThat(updatedAuthor.getTranslations()).doesNotContainKey("uk");
    }

    @Test
    void shouldDeleteAuthor() {
        var author = saveAuthor(a -> a.fullName("Author").country("USA"));

        service.deleteAuthor(author.getId());

        assertThat(testDbClient.findAuthorById(author.getId())).isNull();
    }

    @Test
    void shouldThrowBadRequestWhenDeleteAuthorWithBooks() {
        var author = saveAuthor(a -> a.fullName("Author").country("USA"));
        saveBook(b -> b.title("Book").bookLanguage("English").authors(author));
        var authorId = author.getId();

        assertThatThrownBy(() -> service.deleteAuthor(authorId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.author.has_books");
    }

    @Test
    void shouldDeleteAuthorsBulk() {
        var a1 = saveAuthor(a -> a.fullName("A1").country("USA"));
        var a2 = saveAuthor(a -> a.fullName("A2").country("USA"));

        service.deleteAuthors(List.of(a1.getId(), a2.getId()));

        assertThat(testDbClient.countAuthors()).isZero();
    }

    @Test
    void shouldThrowBadRequestWhenDeleteAuthorsBulkWithBooks() {
        var a1 = saveAuthor(a -> a.fullName("A1").country("USA"));
        var a2 = saveAuthor(a -> a.fullName("A2").country("USA"));
        saveBook(b -> b.title("Book").bookLanguage("English").authors(a1));
        var authorIds = List.of(a1.getId(), a2.getId());

        assertThatThrownBy(() -> service.deleteAuthors(authorIds))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.author.has_books");
    }

}

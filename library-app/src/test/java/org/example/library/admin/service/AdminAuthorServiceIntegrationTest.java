package org.example.library.admin.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.library.admin.dto.AdminAuthorDto;
import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookStatus;
import org.example.library.book.domain.BookTranslation;
import org.example.library.book.repository.BookRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.PostgresTestContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@ContextConfiguration(initializers = PostgresTestContainer.class)
class AdminAuthorServiceIntegrationTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AdminAuthorService authorService;

    @Test
    void shouldReturnAuthorWhenGetById() {
        var author = saveAuthor("Author Name");
        em.flush();
        em.clear();

        var result = authorService.getAuthor(author.getId());

        assertThat(result.getId()).isEqualTo(author.getId());
        assertThat(result.getTranslations().get("en").getFullName()).isEqualTo("Author Name");
    }

    @Test
    void shouldThrowNotFoundWhenGetAuthorByIdWithoutExistingAuthor() {
        assertThatThrownBy(() -> authorService.getAuthor(-1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.author.not_found");
    }

    @Test
    void shouldCreateAuthor() {
        var dto = AdminAuthorDto.builder()
                .birthYear((short) 1950)
                .translations(Map.of("en", AdminAuthorDto.AdminAuthorTranslationDto.builder()
                        .fullName("New Author")
                        .country("USA")
                        .biography("Bio")
                        .build()))
                .build();

        authorService.createAuthor(dto);
        em.flush();
        em.clear();

        var authors = authorRepository.findAll();
        assertThat(authors).hasSize(1);
        var author = authors.get(0);
        assertThat(author.getTranslations().get("en").getFullName()).isEqualTo("New Author");
        assertThat(author.getBirthYear()).isEqualTo((short) 1950);
    }

    @Test
    void shouldUpdateAuthor() {
        var author = saveAuthor("Old Name");
        var dto = AdminAuthorDto.builder()
                .birthYear(author.getBirthYear())
                .deathYear((short) 2000)
                .translations(Map.of("en", AdminAuthorDto.AdminAuthorTranslationDto.builder()
                        .fullName("New Name")
                        .country("USA")
                        .build()))
                .build();
        em.flush();
        em.clear();

        authorService.updateAuthor(author.getId(), dto);
        em.flush();
        em.clear();

        var updatedAuthor = authorRepository.findById(author.getId())
                .orElseThrow(() -> new AssertionError("Author not found after update"));
        assertThat(updatedAuthor.getTranslations().get("en").getFullName()).isEqualTo("New Name");
        assertThat(updatedAuthor.getDeathYear()).isEqualTo((short) 2000);
    }

    @Test
    void shouldDeleteAuthor() {
        var author = saveAuthor("Author");
        em.flush();
        em.clear();

        authorService.deleteAuthor(author.getId());
        em.flush();
        em.clear();

        assertThat(authorRepository.existsById(author.getId())).isFalse();
    }

    @Test
    void shouldThrowBadRequestWhenDeleteAuthorWithBooks() {
        var author = saveAuthor("Author");
        saveBook(Set.of(author));
        em.flush();
        em.clear();

        assertThatThrownBy(() -> authorService.deleteAuthor(author.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.author.has_books");
    }

    @Test
    void shouldDeleteAuthorsBulk() {
        var a1 = saveAuthor("A1");
        var a2 = saveAuthor("A2");
        em.flush();
        em.clear();

        authorService.deleteAuthors(List.of(a1.getId(), a2.getId()));
        em.flush();
        em.clear();

        assertThat(authorRepository.findAll()).isEmpty();
    }

    @Test
    void shouldThrowBadRequestWhenDeleteAuthorsBulkWithBooks() {
        var a1 = saveAuthor("A1");
        var a2 = saveAuthor("A2");
        saveBook(Set.of(a1));
        em.flush();
        em.clear();

        assertThatThrownBy(() -> authorService.deleteAuthors(List.of(a1.getId(), a2.getId())))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.author.has_books");
    }

    private Author saveAuthor(String fullName) {
        var author = Author.builder()
                .birthYear((short) 1900)
                .popularityCount(0)
                .build();

        var translation = AuthorTranslation.builder()
                .languageCode("en")
                .fullName(fullName)
                .country("USA")
                .biography("Biography of " + fullName)
                .author(author)
                .build();
        author.setTranslations(new HashMap<>(Map.of("en", translation)));

        return authorRepository.save(author);
    }

    private void saveBook(Set<Author> authors) {
        var book = Book.builder()
                .category(null)
                .authors(authors)
                .publishYear((short) 2000)
                .pages((short) 200)
                .status(BookStatus.PRELIMINARY)
                .popularityCount(0)
                .build();

        var translation = BookTranslation.builder()
                .languageCode("en")
                .title("Book")
                .bookLanguage("English")
                .description("Desc " + "Book")
                .book(book)
                .build();
        book.setTranslations(new HashMap<>(Map.of("en", translation)));

        bookRepository.save(book);
    }

}

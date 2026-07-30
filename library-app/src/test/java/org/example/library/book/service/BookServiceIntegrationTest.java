package org.example.library.book.service;

import org.example.library.book.dto.BookSearchParams;
import org.example.library.common.pagination.PaginationParams;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private BookService service;

    @Test
    void shouldGetAllBooks() {
        saveBook(b -> b.title("Book 1").bookLanguage("English"));
        saveBook(b -> b.title("Book 2").bookLanguage("English"));

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldFilterBooksByTitle() {
        saveBook(b -> b.title("Spring in Action").bookLanguage("English"));
        saveBook(b -> b.title("Java Persistence with Hibernate").bookLanguage("English"));

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();
        searchParams.setTitle("Spring");

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Spring in Action");
    }

    @Test
    void shouldFindBookWithTypo() {
        saveBook(b -> b.title("The Great Gatsby").bookLanguage("English"));

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();
        searchParams.setTitle("The Great Gatsbyy");

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("The Great Gatsby");
    }

    @Test
    void shouldFilterBooksByCategoryId() {
        var category = saveCategory(c -> c.name("Fiction"));
        var otherCategory = saveCategory(c -> c.name("Sci-Fi"));
        saveBook(b -> b.title("Fictional Story").category(category));
        saveBook(b -> b.title("Sci-Fi Adventure").category(otherCategory));

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();
        searchParams.setCategoryId(category.getId());

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategoryId()).isEqualTo(category.getId());
    }

    @Test
    void shouldFilterBooksByAuthorId() {
        var author = saveAuthor(a -> a.fullName("Author 1"));
        var otherAuthor = saveAuthor(a -> a.fullName("Author 2"));
        saveBook(b -> b.title("Book by A1").authors(author));
        saveBook(b -> b.title("Book by A2").authors(otherAuthor));

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();
        searchParams.setAuthorId(author.getId());

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAuthors()).containsKey(author.getId());
    }

    @Test
    void shouldFilterBooksByPublishYearRange() {
        saveBook(b -> b.title("Old Book").publishYear((short) 1990));
        saveBook(b -> b.title("New Book").publishYear((short) 2020));
        saveBook(b -> b.title("Mid Book").publishYear((short) 2005));

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();
        searchParams.setPublishYearMin((short) 2000);
        searchParams.setPublishYearMax((short) 2010);

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Mid Book");
    }

    @Test
    void shouldFilterBooksByLanguages() {
        saveBook(b -> b.title("English Book").bookLanguage("English"));
        saveBook(b -> b.title("French Book").bookLanguage("French"));

        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new BookSearchParams();
        searchParams.setLanguages(List.of("French"));

        var result = service.getAll(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLanguage()).isEqualTo("French");
    }

    @Test
    void shouldReturnAllLanguages() {
        saveBook(b -> b.title("B1").bookLanguage("English"));
        saveBook(b -> b.title("B2").bookLanguage("English"));
        saveBook(b -> b.title("B3").bookLanguage("French"));

        var languages = service.getAllLanguages();

        assertThat(languages).hasSize(2);
        assertThat(languages).extracting("language").containsExactlyInAnyOrder("English", "French");
        assertThat(languages).filteredOn(l -> l.getLanguage().equals("English"))
                .extracting("count").containsExactly(2L);
    }

}

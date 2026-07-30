package org.example.library.admin.service;

import org.example.library.admin.dto.AdminBookDto;
import org.example.library.book.repository.BookRepository;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.book.domain.BookStatus.PRELIMINARY;

class AdminBookServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AdminBookService bookService;

    @Test
    void shouldReturnBookWhenGetById() {
        var category = saveCategory(c -> c.name("Category"));
        var author = saveAuthor(a -> a.fullName("Author").country("USA"));
        var book = saveBook(b -> b.title("Book Title").bookLanguage("English").category(category).authors(author).publishYear((short)2000).pages((short)200));

        var result = bookService.getBook(book.getId());

        assertThat(result.getId()).isEqualTo(book.getId());
        assertThat(result.getCategoryId()).isEqualTo(category.getId());
        assertThat(result.getAuthorIds()).containsExactly(author.getId());
        assertThat(result.getTranslations().get("en").getTitle()).isEqualTo("Book Title");
    }

    @Test
    void shouldThrowNotFoundWhenGetBookByIdWithoutExistingBook() {
        assertThatThrownBy(() -> bookService.getBook(-1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.book.not_found");
    }

    @Test
    void shouldCreateBook() {
        var category = saveCategory(c -> c.name("Category"));
        var author = saveAuthor(a -> a.fullName("Author").country("USA"));
        var dto = AdminBookDto.builder()
                .categoryId(category.getId())
                .authorIds(List.of(author.getId()))
                .publishYear((short) 2020)
                .pages((short) 300)
                .translations(Map.of("en", AdminBookDto.AdminBookTranslationDto.builder()
                        .title("New Book")
                        .bookLanguage("English")
                        .description("Desc")
                        .build()))
                .build();

        bookService.createBook(dto);

        var books = bookRepository.findAll();
        assertThat(books).hasSize(1);
        var book = testDbClient.findBookById(books.get(0).getId());
        assertThat(book).isNotNull();
        assertThat(book.getCategory().getId()).isEqualTo(category.getId());
        assertThat(testDbClient.existsBookAuthorLink(book.getId(), author.getId())).isTrue();
        assertThat(book.getTranslations().get("en").getTitle()).isEqualTo("New Book");
        assertThat(book.getStatus()).isEqualTo(PRELIMINARY);
    }

    @Test
    void shouldUpdateBook() {
        var book = saveBook(b -> b.title("Old Title").bookLanguage("English"));
        var newCategory = saveCategory(c -> c.name("New Category"));
        var dto = AdminBookDto.builder()
                .categoryId(newCategory.getId())
                .publishYear((short) 2021)
                .pages(book.getPages())
                .translations(Map.of("en", AdminBookDto.AdminBookTranslationDto.builder()
                        .title("New Title")
                        .bookLanguage("English")
                        .build()))
                .build();

        bookService.updateBook(book.getId(), dto);

        var updatedBook = testDbClient.findBookById(book.getId());
        assertThat(updatedBook).isNotNull();
        assertThat(updatedBook.getCategory().getId()).isEqualTo(newCategory.getId());
        assertThat(updatedBook.getTranslations().get("en").getTitle()).isEqualTo("New Title");
        assertThat(updatedBook.getPublishYear()).isEqualTo((short) 2021);
    }

    @Test
    void shouldDeleteBook() {
        var book = saveBook(b -> b.title("Title").bookLanguage("English"));

        bookService.deleteBook(book.getId());

        assertThat(testDbClient.findBookById(book.getId())).isNull();
    }

    @Test
    void shouldDeleteBooksBulk() {
        var b1 = saveBook(b -> b.title("B1").bookLanguage("English"));
        var b2 = saveBook(b -> b.title("B2").bookLanguage("English"));

        bookService.deleteBooks(List.of(b1.getId(), b2.getId()));

        assertThat(testDbClient.countBooks()).isZero();
    }

}

package org.example.library.admin.service;

import org.example.library.admin.dto.AdminBookDto;
import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.book.repository.BookRepository;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.book.domain.BookStatus.PRELIMINARY;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class AdminBookServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AdminBookService bookService;

    @BeforeEach
    void cleanDbBefore() {
        testDbClient.cleanDatabase();
    }

    @AfterEach
    void tearDownEach() {
        testDbClient.cleanDatabase();
    }

    @Test
    void shouldReturnBookWhenGetById() {
        var category = saveCategory("Category");
        var author = saveAuthor();
        var book = saveBook("Book Title", category, Set.of(author));

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
        var category = saveCategory("Category");
        var author = saveAuthor();
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
        var book = saveBook("Old Title", null, Set.of());
        var newCategory = saveCategory("New Category");
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
        var book = saveBook("Title", null, Set.of());

        bookService.deleteBook(book.getId());

        assertThat(testDbClient.findBookById(book.getId())).isNull();
    }

    @Test
    void shouldDeleteBooksBulk() {
        var b1 = saveBook("B1", null, Set.of());
        var b2 = saveBook("B2", null, Set.of());

        bookService.deleteBooks(List.of(b1.getId(), b2.getId()));

        assertThat(testDbClient.countBooks()).isZero();
    }

    private Category saveCategory(String name) {
        var category = Category.builder()
                .popularityCount(0)
                .build();

        var translation = CategoryTranslation.builder()
                .languageCode("en")
                .name(name)
                .description("Desc " + name)
                .category(category)
                .build();
        category.setTranslations(new HashMap<>(Map.of("en", translation)));

        testDbClient.saveCategory(category);
        return category;
    }

    private Author saveAuthor() {
        var author = Author.builder()
                .birthYear((short) 1900)
                .popularityCount(0)
                .build();

        var translation = AuthorTranslation.builder()
                .languageCode("en")
                .fullName("Author")
                .country("USA")
                .biography("Biography of Author")
                .author(author)
                .build();
        author.setTranslations(new HashMap<>(Map.of("en", translation)));

        testDbClient.saveAuthor(author);
        return author;
    }

    private Book saveBook(String title, Category category, Set<Author> authors) {
        var book = Book.builder()
                .category(category)
                .authors(authors)
                .publishYear((short) 2000)
                .pages((short) 200)
                .status(PRELIMINARY)
                .popularityCount(0)
                .build();

        var translation = BookTranslation.builder()
                .languageCode("en")
                .title(title)
                .bookLanguage("English")
                .description("Desc " + title)
                .book(book)
                .build();
        book.setTranslations(new HashMap<>(Map.of("en", translation)));

        testDbClient.saveBook(book);
        if (authors != null) {
            for (Author author : authors) {
                testDbClient.linkBookToAuthor(book.getId(), author.getId());
            }
        }

        return book;
    }

}

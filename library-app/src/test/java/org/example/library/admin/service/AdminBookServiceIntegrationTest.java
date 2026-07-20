package org.example.library.admin.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.library.admin.dto.AdminBookDto;
import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookStatus;
import org.example.library.book.domain.BookTranslation;
import org.example.library.book.repository.BookRepository;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.category.repository.CategoryRepository;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.PostgresTestContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
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
class AdminBookServiceIntegrationTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AdminBookService bookService;

    @DynamicPropertySource
    static void setPostgresProperties(DynamicPropertyRegistry registry) {
        PostgresTestContainer.setProperties(registry);
    }

    @Test
    void shouldReturnBookWhenGetById() {
        var category = saveCategory("Category");
        var author = saveAuthor();
        var book = saveBook("Book Title", category, Set.of(author));
        em.flush();
        em.clear();

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
        em.flush();
        em.clear();

        var books = bookRepository.findAll();
        assertThat(books).hasSize(1);
        var book = books.get(0);
        assertThat(book.getCategory().getId()).isEqualTo(category.getId());
        assertThat(book.getAuthors()).hasSize(1);
        assertThat(book.getTranslations().get("en").getTitle()).isEqualTo("New Book");
        assertThat(book.getStatus()).isEqualTo(BookStatus.PRELIMINARY);
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
        em.flush();
        em.clear();

        var updatedBook = bookRepository.findById(book.getId())
                .orElseThrow(() -> new AssertionError("Book not found after update"));
        assertThat(updatedBook.getCategory().getId()).isEqualTo(newCategory.getId());
        assertThat(updatedBook.getTranslations().get("en").getTitle()).isEqualTo("New Title");
        assertThat(updatedBook.getPublishYear()).isEqualTo((short) 2021);
    }

    @Test
    void shouldDeleteBook() {
        var book = saveBook("Title", null, Set.of());
        em.flush();
        em.clear();

        bookService.deleteBook(book.getId());
        em.flush();
        em.clear();

        assertThat(bookRepository.existsById(book.getId())).isFalse();
    }

    @Test
    void shouldDeleteBooksBulk() {
        var b1 = saveBook("B1", null, Set.of());
        var b2 = saveBook("B2", null, Set.of());
        em.flush();
        em.clear();

        bookService.deleteBooks(List.of(b1.getId(), b2.getId()));
        em.flush();
        em.clear();

        assertThat(bookRepository.findAll()).isEmpty();
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
        return categoryRepository.save(category);
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
                .biography("Biography of " + "Author")
                .author(author)
                .build();
        author.setTranslations(new HashMap<>(Map.of("en", translation)));

        return authorRepository.save(author);
    }

    private Book saveBook(String title, Category category, Set<Author> authors) {
        var book = Book.builder()
                .category(category)
                .authors(authors)
                .publishYear((short) 2000)
                .pages((short) 200)
                .status(BookStatus.PRELIMINARY)
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
        return bookRepository.save(book);
    }

}

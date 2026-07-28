package org.example.library.recommendation.service;

import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.book.repository.BookRepository;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.category.repository.CategoryRepository;
import org.example.library.config.PostgresTestContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.example.library.book.domain.BookStatus.SYNCED;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class BatchEmbeddingProcessorIntegrationTest {

    @Autowired
    private BatchEmbeddingProcessor batchEmbeddingProcessor;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category defaultCategory;

    @BeforeAll
    static void setUpAll() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @BeforeEach
    void setUp() {
        var translation = CategoryTranslation.builder()
                .languageCode("en")
                .name("IT")
                .description("IT Category")
                .build();
        defaultCategory = Category.builder()
                .popularityCount(0)
                .translations(Map.of("en", translation))
                .build();
        translation.setCategory(defaultCategory);

        categoryRepository.save(defaultCategory);
    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldProcessBatchAndPersistChanges() {
        var book1 = createBook("Book 1", "Desc 1");
        var book2 = createBook("Book 2", "Desc 2");
        bookRepository.saveAll(List.of(book1, book2));

        batchEmbeddingProcessor.processBatch(PageRequest.of(0, 10));

        var updatedBooks = bookRepository.findAll();
        assertThat(updatedBooks).hasSize(2);
        assertThat(updatedBooks).allMatch(b -> b.getEmbedding() != null);
        assertThat(updatedBooks).allMatch(b -> b.getEmbedding().length == 384);
        assertThat(updatedBooks).allMatch(b -> b.getStatus() == SYNCED);
    }

    private Book createBook(String title, String description) {
        var book = Book.builder()
                .category(defaultCategory)
                .publishYear((short) 2020)
                .pages((short) 100)
                .coverImageUrl("url")
                .popularityCount(0)
                .status(NEW)
                .build();
        var translation = BookTranslation.builder()
                .languageCode("en")
                .title(title)
                .bookLanguage("English")
                .description(description)
                .book(book)
                .build();
        book.setTranslations(Map.of("en", translation));

        return book;
    }

}

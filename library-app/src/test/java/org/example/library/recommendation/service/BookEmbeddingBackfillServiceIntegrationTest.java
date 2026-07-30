package org.example.library.recommendation.service;

import org.example.library.book.repository.BookRepository;
import org.example.library.category.repository.CategoryRepository;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.example.library.book.domain.BookStatus.SYNCED;

@TestPropertySource(properties = {
        "recommendations.trigger.count=2",
        "recommendations.rebuild.batch-size=1"
})
class BookEmbeddingBackfillServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private BookEmbeddingBackfillService bookEmbeddingBackfillService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void shouldUpdateBooksMissingEmbeddings() {
        var defaultCategory = saveCategory(c -> c.name("IT").description("IT Category"));
        saveBook(b -> b.title("Only Book").bookLanguage("English").description("Description").category(defaultCategory).status(NEW));

        bookEmbeddingBackfillService.backfillEmbeddings();

        var books = bookRepository.findAll();
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getStatus()).isEqualTo(SYNCED);
        assertThat(books.get(0).getEmbedding()).isNotNull();
        assertThat(books.get(0).getEmbedding()).hasSize(384);
    }

    @Test
    void shouldNotUpdateBooksThatAlreadyHaveEmbeddings() {
        var defaultCategory = saveCategory(c -> c.name("IT").description("IT Category"));
        float[] existingEmbedding = new float[384];
        existingEmbedding[0] = 0.5f;
        saveBook(b -> b.title("Existing Book").bookLanguage("English").description("Description").embedding(existingEmbedding).category(defaultCategory).status(SYNCED));

        bookEmbeddingBackfillService.backfillEmbeddings();

        var books = bookRepository.findAll();
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getEmbedding()).isEqualTo(existingEmbedding);
    }

    @Test
    void shouldProcessMultipleBatchesCorrectly() {
        var defaultCategory = saveCategory(c -> c.name("IT").description("IT Category"));
        saveBook(b -> b.title("Book 1").bookLanguage("English").description("Desc 1").category(defaultCategory).status(NEW));
        saveBook(b -> b.title("Book 2").bookLanguage("English").description("Desc 2").category(defaultCategory).status(NEW));
        saveBook(b -> b.title("Book 3").bookLanguage("English").description("Desc 3").category(defaultCategory).status(NEW));

        bookEmbeddingBackfillService.backfillEmbeddings();

        var books = bookRepository.findAll();
        assertThat(books).hasSize(3);
        assertThat(books).allMatch(b -> b.getEmbedding() != null);
        assertThat(books).allMatch(b -> b.getStatus() == SYNCED);
    }

    @Test
    void shouldUpdateOnlyBooksWithoutEmbeddingsInMixedScenario() {
        var defaultCategory = saveCategory(c -> c.name("IT").description("IT Category"));
        float[] existingEmbedding = new float[384];
        existingEmbedding[0] = 0.7f;
        saveBook(b -> b.title("Has Embedding").bookLanguage("English").description("Desc").embedding(existingEmbedding).category(defaultCategory).status(SYNCED));
        saveBook(b -> b.title("No Embedding").bookLanguage("English").description("Desc").category(defaultCategory).status(NEW));

        bookEmbeddingBackfillService.backfillEmbeddings();

        transactionTemplate.executeWithoutResult(status -> {
            var allBooks = bookRepository.findAll();
            var bookWithEmbedding = allBooks.stream()
                    .filter(b -> b.getTranslations().get("en").getTitle().equals("Has Embedding"))
                    .findFirst().orElseThrow();
            var bookWithoutEmbedding = allBooks.stream()
                    .filter(b -> b.getTranslations().get("en").getTitle().equals("No Embedding"))
                    .findFirst().orElseThrow();
            assertThat(bookWithEmbedding.getEmbedding()).isEqualTo(existingEmbedding);
            assertThat(bookWithoutEmbedding.getEmbedding()).isNotNull();
            assertThat(bookWithoutEmbedding.getStatus()).isEqualTo(SYNCED);
        });
    }

}

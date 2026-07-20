package org.example.library.book.repository;

import org.example.library.author.domain.Author;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookTranslation;
import org.example.library.book.dto.LanguageWithCount;
import org.example.library.category.domain.Category;
import org.example.library.config.AbstractRepositoryTest;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.example.library.book.domain.BookStatus.SYNCED;
import static org.example.library.config.EntityRecursiveComparisonConfigs.BOOK_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.BOOK_SAVED;
import static org.example.library.config.EntityRecursiveComparisonConfigs.BOOK_TRANSLATION_SAVED;
import static org.example.library.config.EntityRecursiveComparisonConfigs.CATEGORY_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.USER_DIRECT_FIELDS;
import static org.example.library.user.domain.Role.USER;

class BookRepositoryTest extends AbstractRepositoryTest<BookRepository> {

    @Test
    void save_ShouldPersistBook_AndNotCascadeOwnerOrCategory() {
        User owner = createOwner();
        testDbClient.saveUser(owner);
        Category category = createCategory();
        testDbClient.saveCategory(category);
        long initialOwnerCount = testDbClient.countUsers();
        long initialCategoryCount = testDbClient.countCategories();
        Book expected = createBook(owner, category);

        Book actual = repository.save(expected);

        assertThat(actual)
                .usingRecursiveComparison(BOOK_SAVED)
                .isEqualTo(expected);
        Book dbState = testDbClient.findBookById(actual.getId());
        assertThat(dbState)
                .isNotNull()
                .usingRecursiveComparison(BOOK_DIRECT_FIELDS)
                .isEqualTo(actual);
        assertThat(dbState.getTranslations()).hasSize(1);
        BookTranslation actualTranslation = actual.getTranslations().get("en");
        BookTranslation dbTranslation = dbState.getTranslations().get("en");
        assertThat(dbTranslation)
                .isNotNull()
                .usingRecursiveComparison(BOOK_TRANSLATION_SAVED)
                .isEqualTo(actualTranslation);
        assertThat(testDbClient.countUsers()).isEqualTo(initialOwnerCount);
        assertThat(testDbClient.countCategories()).isEqualTo(initialCategoryCount);
    }

    @Test
    @Transactional
    void findById_ShouldReturnBook_WhenBookExists() {
        User owner = createOwner();
        testDbClient.saveUser(owner);
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(owner, category);
        testDbClient.saveBook(book);

        Optional<Book> actual = repository.findById(book.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(BOOK_DIRECT_FIELDS)
                .isEqualTo(book);
        assertThat(actual.get().getOwner())
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(USER_DIRECT_FIELDS)
                .isEqualTo(owner);
        assertThat(actual.get().getCategory())
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(CATEGORY_DIRECT_FIELDS)
                .isEqualTo(category);
        assertThat(actual.get().getTranslations()).hasSize(1);
        assertThat(actual.get().getTranslations().get("en"))
                .usingRecursiveComparison(BOOK_TRANSLATION_SAVED)
                .isEqualTo(book.getTranslations().get("en"));
    }

    @Test
    void delete_ShouldRemoveBook_AndCascadeTranslations_ButKeepOwnerAndCategory() {
        User owner = createOwner();
        testDbClient.saveUser(owner);
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(owner, category);
        testDbClient.saveBook(book);
        long initialOwnerCount = testDbClient.countUsers();
        long initialCategoryCount = testDbClient.countCategories();

        repository.deleteById(book.getId());

        assertThat(testDbClient.findBookById(book.getId())).isNull();
        assertThat(testDbClient.countBookTranslations()).isZero();
        assertThat(testDbClient.countUsers()).isEqualTo(initialOwnerCount);
        assertThat(testDbClient.countCategories()).isEqualTo(initialCategoryCount);
    }

    @Test
    void findAllLanguagesWithCount_ShouldReturnLanguageCounts() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book1 = createBook(null, category);
        testDbClient.saveBook(book1);
        Book book2 = createBook(null, category);
        book2.getTranslations().get("en").setBookLanguage("French");
        testDbClient.saveBook(book2);

        List<LanguageWithCount> actual = repository.findAllLanguagesWithCount("en");

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getLanguage()).isEqualTo("English");
        assertThat(actual.get(0).getCount()).isEqualTo(1L);
        assertThat(actual.get(1).getLanguage()).isEqualTo("French");
        assertThat(actual.get(1).getCount()).isEqualTo(1L);
    }

    @Test
    void findEmbeddingById_ShouldReturnBook_WhenEmbeddingExists() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(null, category);
        testDbClient.saveBook(book);

        Optional<Book> actual = repository.findEmbeddingById(book.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getEmbedding()).hasSize(384);
        assertThat(actual.get().getEmbedding()[0]).isEqualTo(0.1f);
        assertThat(actual.get().getEmbedding()[1]).isEqualTo(0.2f);
        assertThat(actual.get().getEmbedding()[2]).isEqualTo(0.3f);
    }

    @Test
    void countWhereBookStatusNot_ShouldReturnCount() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book1 = createBook(null, category);
        testDbClient.saveBook(book1);
        Book book2 = createBook(null, category);
        book2.setStatus(SYNCED);
        testDbClient.saveBook(book2);

        long actual = repository.countWhereBookStatusNot(SYNCED);

        assertThat(actual).isEqualTo(1L);
    }

    @Test
    void countBooksWithoutEmbedding_ShouldReturnCount() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book1 = createBook(null, category);
        testDbClient.saveBook(book1);
        Book book2 = createBook(null, category);
        book2.setEmbedding(null);
        testDbClient.saveBook(book2);

        long actual = repository.countBooksWithoutEmbedding();

        assertThat(actual).isEqualTo(1L);
    }

    @Test
    void findBooksWithoutEmbedding_ShouldReturnBooks() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(null, category);
        book.setEmbedding(null);
        testDbClient.saveBook(book);

        Page<Book> actual = repository.findBooksWithoutEmbedding(PageRequest.of(0, 10));

        assertThat(actual.getContent()).hasSize(1);
        Book dbBook = actual.getContent().get(0);
        assertThat(dbBook.getCategory())
                .usingRecursiveComparison(CATEGORY_DIRECT_FIELDS)
                .isEqualTo(category);
    }

    @Test
    void incrementPopularityCount_ShouldIncrementPopularity() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(null, category);
        testDbClient.saveBook(book);

        transactionTemplate.executeWithoutResult(status -> repository.incrementPopularityCount(List.of(book.getId())));

        Book dbState = testDbClient.findBookById(book.getId());
        assertThat(dbState.getPopularityCount()).isEqualTo(1);
    }

    @Test
    void decrementPopularityCount_ShouldDecrementPopularity() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(null, category);
        book.setPopularityCount(5);
        testDbClient.saveBook(book);

        transactionTemplate.executeWithoutResult(status -> repository.decrementPopularityCount(List.of(book.getId())));

        Book dbState = testDbClient.findBookById(book.getId());
        assertThat(dbState.getPopularityCount()).isEqualTo(4);
    }

    @Test
    void existsByAuthorsId_ShouldReturnBoolean() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(null, category);
        testDbClient.saveBook(book);
        Author author = createAuthor();
        testDbClient.saveAuthor(author);
        testDbClient.linkBookToAuthor(book.getId(), author.getId());

        boolean actual = repository.existsByAuthorsId(author.getId());

        assertThat(actual).isTrue();
    }

    @Test
    void existsByAuthorsIdIn_ShouldReturnBoolean() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(null, category);
        testDbClient.saveBook(book);
        Author author = createAuthor();
        testDbClient.saveAuthor(author);
        testDbClient.linkBookToAuthor(book.getId(), author.getId());

        boolean actual = repository.existsByAuthorsIdIn(List.of(author.getId()));

        assertThat(actual).isTrue();
    }

    @Test
    void existsByCategoryId_ShouldReturnBoolean() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(null, category);
        testDbClient.saveBook(book);

        boolean actual = repository.existsByCategoryId(category.getId());

        assertThat(actual).isTrue();
    }

    @Test
    void existsByCategoryIdIn_ShouldReturnBoolean() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(null, category);
        testDbClient.saveBook(book);

        boolean actual = repository.existsByCategoryIdIn(List.of(category.getId()));

        assertThat(actual).isTrue();
    }

    @Test
    void findAll_ShouldReturnBooksWithEagerlyLoadedCategory_WhenSpecificationProvided() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(null, category);
        testDbClient.saveBook(book);
        Specification<Book> spec = (root, query, cb) -> cb.equal(root.get("id"), book.getId());

        Page<Book> actual = repository.findAll(spec, PageRequest.of(0, 10));

        assertThat(actual.getContent()).hasSize(1);
        Book dbBook = actual.getContent().get(0);
        assertThat(dbBook.getCategory())
                .usingRecursiveComparison(CATEGORY_DIRECT_FIELDS)
                .isEqualTo(category);
    }

    @Test
    void save_ShouldPersistBook_AndLinkAuthor() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Author author = createAuthor();
        testDbClient.saveAuthor(author);
        Book expected = createBook(null, category);
        expected.getAuthors().add(author);

        Book actual = repository.save(expected);

        assertThat(actual)
                .usingRecursiveComparison(BOOK_SAVED)
                .isEqualTo(expected);
        assertThat(testDbClient.existsBookAuthorLink(actual.getId(), author.getId())).isTrue();
    }

    @Test
    void delete_ShouldRemoveBook_AndCascadeToBookAuthors_ButKeepAuthor() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Author author = createAuthor();
        testDbClient.saveAuthor(author);
        Book book = createBook(null, category);
        testDbClient.saveBook(book);
        testDbClient.linkBookToAuthor(book.getId(), author.getId());

        repository.deleteById(book.getId());

        assertThat(testDbClient.findBookById(book.getId())).isNull();
        assertThat(testDbClient.existsBookAuthorLink(book.getId(), author.getId())).isFalse();
        assertThat(testDbClient.findAuthorById(author.getId())).isNotNull();
    }

    private User createOwner() {
        return User.builder()
                .email("owner@example.com")
                .fullName("Book Owner")
                .password("pass")
                .role(USER)
                .build();
    }

    private Category createCategory() {
        return Category.builder()
                .popularityCount(0)
                .build();
    }

    private Book createBook(User owner, Category category) {
        Book book = Book.builder()
                .owner(owner)
                .category(category)
                .publishYear((short) 2010)
                .pages((short) 400)
                .coverImageUrl("http://example.com/cover.png")
                .embedding(createEmbedding())
                .status(NEW)
                .popularityCount(0)
                .build();

        BookTranslation translation = BookTranslation.builder()
                .languageCode("en")
                .title("A Great Book")
                .bookLanguage("English")
                .description("Book description")
                .book(book)
                .build();
        book.getTranslations().put("en", translation);

        return book;
    }

    private Author createAuthor() {
        return Author.builder()
                .birthYear((short) 1950)
                .deathYear((short) 2010)
                .popularityCount(0)
                .build();
    }

    private float[] createEmbedding() {
        float[] embedding = new float[384];
        embedding[0] = 0.1f;
        embedding[1] = 0.2f;
        embedding[2] = 0.3f;

        return embedding;
    }

}

package org.example.library.category.repository;

import org.example.library.book.domain.Book;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.category.dto.CategoryWithBooksCount;
import org.example.library.config.AbstractRepositoryTest;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.example.library.config.EntityRecursiveComparisonConfigs.BOOK_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.CATEGORY_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.CATEGORY_SAVED;
import static org.example.library.config.EntityRecursiveComparisonConfigs.CATEGORY_TRANSLATION_SAVED;
import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;
import static org.example.library.user.domain.Role.USER;

class CategoryRepositoryTest extends AbstractRepositoryTest<CategoryRepository> {

    @Test
    void save_ShouldPersistCategory_AndCascadeTranslations() {
        Category expected = createCategory();

        Category actual = repository.save(expected);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual)
                .usingRecursiveComparison(CATEGORY_SAVED)
                .isEqualTo(expected);
        Category dbState = testDbClient.findCategoryById(actual.getId());
        assertThat(dbState)
                .isNotNull()
                .usingRecursiveComparison(CATEGORY_DIRECT_FIELDS)
                .isEqualTo(actual);
        assertThat(dbState.getTranslations()).hasSize(1);
        CategoryTranslation actualTranslation = actual.getTranslations().get("en");
        CategoryTranslation dbTranslation = dbState.getTranslations().get("en");
        assertThat(dbTranslation)
                .isNotNull()
                .usingRecursiveComparison(CATEGORY_TRANSLATION_SAVED)
                .isEqualTo(actualTranslation);
    }

    @Test
    @Transactional
    void findById_ShouldReturnCategory_WhenCategoryExists() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(category);
        testDbClient.saveBook(book);

        Optional<Category> actual = repository.findById(category.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(CATEGORY_DIRECT_FIELDS)
                .isEqualTo(category);
        assertThat(actual.get().getTranslations()).hasSize(1);
        assertThat(actual.get().getTranslations().get("en"))
                .usingRecursiveComparison(CATEGORY_TRANSLATION_SAVED)
                .isEqualTo(category.getTranslations().get("en"));
        assertThat(actual.get().getBooks()).hasSize(1);
        assertThat(actual.get().getBooks().get(0))
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(BOOK_DIRECT_FIELDS)
                .isEqualTo(book);
    }

    @Test
    void delete_ShouldRemoveCategory_AndCascadeTranslations() {
        Category category = createCategory();
        testDbClient.saveCategory(category);

        repository.deleteById(category.getId());

        assertThat(testDbClient.findCategoryById(category.getId())).isNull();
        assertThat(testDbClient.countCategoryTranslations()).isZero();
    }

    @Test
    void findAllIds_ShouldReturnAllIds() {
        Category category1 = createCategory();
        testDbClient.saveCategory(category1);
        Category category2 = createCategory();
        testDbClient.saveCategory(category2);

        Set<Integer> actual = repository.findAllIds();

        assertThat(actual).containsExactlyInAnyOrder(category1.getId(), category2.getId());
    }

    @Test
    void searchWithBooksCount_ShouldReturnPaginatedCategoriesWithBooksCount() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(category);
        testDbClient.saveBook(book);

        Page<CategoryWithBooksCount> actual = repository.searchWithBooksCount("Sci-Fi", null, null, "en", PageRequest.of(0, 10));

        assertThat(actual.getContent()).hasSize(1);
        CategoryWithBooksCount dto = actual.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(category.getId());
        assertThat(dto.getName()).isEqualTo("Sci-Fi");
        assertThat(dto.getBooksCount()).isEqualTo(1L);
    }

    @Test
    void incrementPopularityCountByBookIds_ShouldIncrementPopularity() {
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(category);
        testDbClient.saveBook(book);

        transactionTemplate.executeWithoutResult(status -> repository.incrementPopularityCountByBookIds(List.of(book.getId())));

        Category dbState = testDbClient.findCategoryById(category.getId());
        assertThat(dbState.getPopularityCount()).isEqualTo(1);
    }

    @Test
    void decrementPopularityCountByBookIds_ShouldDecrementPopularity() {
        Category category = createCategory();
        category.setPopularityCount(5);
        testDbClient.saveCategory(category);
        Book book = createBook(category);
        testDbClient.saveBook(book);

        transactionTemplate.executeWithoutResult(status -> repository.decrementPopularityCountByBookIds(List.of(book.getId())));

        Category dbState = testDbClient.findCategoryById(category.getId());
        assertThat(dbState.getPopularityCount()).isEqualTo(4);
    }

    @Test
    void searchForUser_ShouldReturnCategoriesWithBooksCountForSpecificUser() {
        User user = User.builder()
                .email("user-category@example.com")
                .fullName("User Category")
                .password("password")
                .role(USER)
                .build();
        testDbClient.saveUser(user);
        Category category = createCategory();
        testDbClient.saveCategory(category);
        Book book = createBook(category);
        testDbClient.saveBook(book);
        LibraryBook libraryBook = LibraryBook.builder()
                .book(book)
                .user(user)
                .status(NO_TAG)
                .build();
        testDbClient.saveLibraryBook(libraryBook);

        Page<CategoryWithBooksCount> actual = repository.searchForUser(user.getId(), "Sci-Fi", null, null, "en", PageRequest.of(0, 10));

        assertThat(actual.getContent()).hasSize(1);
        CategoryWithBooksCount dto = actual.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(category.getId());
        assertThat(dto.getBooksCount()).isEqualTo(1L);
    }

    private Category createCategory() {
        Category category = Category.builder()
                .popularityCount(0)
                .build();

        CategoryTranslation translation = CategoryTranslation.builder()
                .languageCode("en")
                .name("Sci-Fi")
                .description("Science Fiction")
                .category(category)
                .build();
        category.getTranslations().put("en", translation);

        return category;
    }

    private Book createBook(Category category) {
        return Book.builder()
                .category(category)
                .publishYear((short) 2000)
                .pages((short) 350)
                .coverImageUrl("http://example.com/cover.png")
                .status(NEW)
                .popularityCount(0)
                .build();
    }

}

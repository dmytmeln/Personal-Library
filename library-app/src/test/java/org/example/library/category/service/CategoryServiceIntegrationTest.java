package org.example.library.category.service;

import org.example.library.category.dto.CategorySearchParams;
import org.example.library.common.exception.NotFoundException;
import org.example.library.common.pagination.PaginationParams;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private CategoryService service;

    @Test
    void shouldReturnCategoryWhenGetById() {
        var expected = saveCategory(c -> c.name("Test Category"));

        var existingCategory = service.getById(expected.getId());

        assertThat(existingCategory.name()).isEqualTo(expected.getTranslations().get("en").getName());
        assertThat(existingCategory.description()).isEqualTo(expected.getTranslations().get("en").getDescription());
    }

    @Test
    void shouldThrowNotFoundWhenGetByIdWithoutExistingCategory() {
        assertThatThrownBy(() -> service.getById(-99999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.category.not_found");
    }

    @Test
    void shouldSearchCategoriesWithBooksCount() {
        var category = saveCategory(c -> c.name("Fiction"));
        saveBook(b -> b.title("Book").bookLanguage("English").category(category));
        saveBook(b -> b.title("Book").bookLanguage("English").category(category));
        var otherCategory = saveCategory(c -> c.name("Science"));
        saveBook(b -> b.title("Book").bookLanguage("English").category(otherCategory));
        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setSort(List.of("name;asc"));
        var searchParams = new CategorySearchParams();
        searchParams.setName("Fiction");

        var result = service.search(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Fiction");
        assertThat(result.getContent().get(0).getBooksCount()).isEqualTo(2);
    }

    @Test
    void shouldFindCategoryWithTypo() {
        saveCategory(c -> c.name("History"));
        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        var searchParams = new CategorySearchParams();
        searchParams.setName("Histoy");

        var result = service.search(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("History");
    }

    @Test
    void shouldSearchCategoriesByBooksCountRange() {
        var cat1 = saveCategory(c -> c.name("Cat 1"));
        saveBook(b -> b.title("Book").bookLanguage("English").category(cat1));
        var cat2 = saveCategory(c -> c.name("Cat 2"));
        saveBook(b -> b.title("Book").bookLanguage("English").category(cat2));
        saveBook(b -> b.title("Book").bookLanguage("English").category(cat2));
        var cat3 = saveCategory(c -> c.name("Cat 3"));
        saveBook(b -> b.title("Book").bookLanguage("English").category(cat3));
        saveBook(b -> b.title("Book").bookLanguage("English").category(cat3));
        saveBook(b -> b.title("Book").bookLanguage("English").category(cat3));
        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setSort(List.of("name;asc"));
        var searchParams = new CategorySearchParams();
        searchParams.setBooksCountMin(2);
        searchParams.setBooksCountMax(2);

        var result = service.search(pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Cat 2");
        assertThat(result.getContent().get(0).getBooksCount()).isEqualTo(2);
    }

    @Test
    void shouldSearchCategoriesForUser() {
        var user = saveUser();
        var otherUser = saveUser(u -> u.email("other@example.com").fullName("Other User"));
        var category = saveCategory(c -> c.name("User Category"));
        var book1 = saveBook(b -> b.title("Book").bookLanguage("English").category(category));
        var book2 = saveBook(b -> b.title("Book").bookLanguage("English").category(category));
        var book3 = saveBook(b -> b.title("Book").bookLanguage("English").category(category));
        saveLibraryBook(lb -> lb.user(user).book(book1));
        saveLibraryBook(lb -> lb.user(user).book(book2));
        saveLibraryBook(lb -> lb.user(otherUser).book(book3));
        var pagination = new PaginationParams();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setSort(List.of("name;asc"));
        var searchParams = new CategorySearchParams();

        var result = service.searchForUser(user.getId(), pagination, searchParams);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("User Category");
        assertThat(result.getContent().get(0).getBooksCount()).isEqualTo(2);
    }

}

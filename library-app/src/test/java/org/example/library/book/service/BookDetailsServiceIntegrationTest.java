package org.example.library.book.service;

import org.example.library.book.dto.GlobalBookDetails;
import org.example.library.book.dto.LibraryBookDetails;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.library_book.domain.LibraryBookStatus.READING;

class BookDetailsServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private BookDetailsService service;

    @Test
    void shouldReturnDetailsWithLibraryBookWhenInUserLibrary() {
        var user = saveUser(u -> u.email("test@example.com"));
        var author = saveAuthor(a -> a.fullName("Author Name").country("Country"));
        var category = saveCategory(c -> c.name("Category Name"));
        var book = saveBook(b -> b.title("Book Title").bookLanguage("en").authors(author).category(category));
        saveLibraryBook(lb -> lb.user(user).book(book).title("User Title").status(READING));

        var details = service.getDetails(book.getId(), user.getId());

        assertThat(details).isInstanceOf(LibraryBookDetails.class);
        var libraryDetails = (LibraryBookDetails) details;
        assertThat(libraryDetails.getLibraryBook()).isNotNull();
        assertThat(libraryDetails.getLibraryBook().getBook().getTitle()).isEqualTo("User Title");
        assertThat(libraryDetails.getAverageRating()).isZero();
        assertThat(libraryDetails.getRatingsNumber()).isZero();
    }

    @Test
    void shouldReturnDetailsWithBookWhenNotInUserLibrary() {
        var user = saveUser(u -> u.email("other@example.com"));
        var author = saveAuthor(a -> a.fullName("Author Name").country("Country"));
        var category = saveCategory(c -> c.name("Category Name"));
        var book = saveBook(b -> b.title("Book Title").bookLanguage("en").authors(author).category(category));

        var details = service.getDetails(book.getId(), user.getId());

        assertThat(details).isInstanceOf(GlobalBookDetails.class);
        var globalDetails = (GlobalBookDetails) details;
        assertThat(globalDetails.getBook()).isNotNull();
        assertThat(globalDetails.getBook().getTitle()).isEqualTo("Book Title");
    }

    @Test
    void shouldReturnDetailsWithAverageRating() {
        var user1 = saveUser(u -> u.email("user1@example.com"));
        var user2 = saveUser(u -> u.email("user2@example.com"));
        var author = saveAuthor(a -> a.fullName("Author Name").country("Country"));
        var category = saveCategory(c -> c.name("Category Name"));
        var book = saveBook(b -> b.title("Rated Book").bookLanguage("en").authors(author).category(category));
        saveLibraryBook(lb -> lb.user(user1).book(book).title("Title 1").rating((byte) 5).status(READING));
        saveLibraryBook(lb -> lb.user(user2).book(book).title("Title 2").rating((byte) 3).status(READING));

        var details = service.getDetails(book.getId(), user1.getId());

        assertThat(details.getAverageRating()).isEqualTo(4.0);
        assertThat(details.getRatingsNumber()).isEqualTo(2L);
    }

    @Test
    void shouldThrowNotFoundWhenBookDoesNotExist() {
        var user = saveUser(u -> u.email("test@example.com"));
        var userId = user.getId();

        assertThatThrownBy(() -> service.getDetails(-999, userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.book.not_found");
    }

}

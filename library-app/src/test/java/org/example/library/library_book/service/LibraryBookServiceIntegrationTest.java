package org.example.library.library_book.service;

import org.example.library.common.pagination.PaginationParams;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.example.library.library_book.dto.CreateLocalBookDto;
import org.example.library.library_book.dto.LibraryBookSearchCriteria;
import org.example.library.library_book.repository.LibraryBookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;
import static org.example.library.library_book.domain.LibraryBookStatus.READ;
import static org.example.library.library_book.domain.LibraryBookStatus.READING;
import static org.example.library.library_book.domain.LibraryBookStatus.TO_READ;
import static org.example.library.note.domain.Note.NoteType.TEXT;

class LibraryBookServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private LibraryBookRepository repository;

    @Autowired
    private LibraryBookService service;

    @Test
    void shouldGetAllByUserId() {
        var user = saveUser();
        var book = saveBook(b -> b.title("Global Book"));
        saveLibraryBook(lb -> lb.user(user).book(book));
        var criteria = new LibraryBookSearchCriteria();
        var pagination = new PaginationParams();

        var result = service.getAllByUserId(user.getId(), criteria, pagination);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBook().getTitle()).isEqualTo("Global Book");
    }

    @Test
    void shouldCreateLocalBook() {
        var user = saveUser();
        var dto = CreateLocalBookDto.builder()
                .title("Local Book")
                .description("Local Description")
                .bookLanguage("uk")
                .status(READING)
                .build();

        service.createLocalBook(dto, user.getId());

        var libraryBooks = repository.findAllByUserIdWithBook(user.getId());
        assertThat(libraryBooks).hasSize(1);
        var saved = libraryBooks.get(0);
        assertThat(saved.getTitle()).isEqualTo("Local Book");
        assertThat(saved.getStatus()).isEqualTo(READING);
        assertThat(saved.getBook().getOwner().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldAddExistingBookToLibrary() {
        var user = saveUser();
        var book = saveBook(b -> b.title("Existing Book"));

        service.create(book.getId(), user.getId());

        var libraryBooks = repository.findAllByUserIdWithBook(user.getId());
        assertThat(libraryBooks).hasSize(1);
        assertThat(libraryBooks.get(0).getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    void shouldRateLibraryBook() {
        var user = saveUser();
        var book = saveBook(b -> b.title("Book to Rate"));
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));

        var result = service.rate(libraryBook.getId(), user.getId(), 5);

        assertThat(result.getRating()).isEqualTo((byte) 5);
        var updated = testDbClient.findLibraryBookById(libraryBook.getId());
        assertThat(updated).isNotNull();
        assertThat(updated.getRating()).isEqualTo((byte) 5);
    }

    @Test
    void shouldUpdateStatus() {
        var user = saveUser();
        var book = saveBook(b -> b.title("Book Status"));
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book).status(NO_TAG));

        var result = service.updateStatus(libraryBook.getId(), user.getId(), READ);

        assertThat(result.getStatus()).isEqualTo(READ.name());
        var updated = testDbClient.findLibraryBookById(libraryBook.getId());
        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(READ);
    }

    @Test
    void shouldDeleteLibraryBook() {
        var user = saveUser();
        var book = saveBook(b -> b.title("Book to Delete"));
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));

        service.delete(libraryBook.getId(), user.getId());

        assertThat(testDbClient.findLibraryBookById(libraryBook.getId())).isNull();
    }

    @Test
    void shouldBulkAddBooks() {
        var user = saveUser();
        var book1 = saveBook(b -> b.title("Book 1"));
        var book2 = saveBook(b -> b.title("Book 2"));
        var book3 = saveBook(b -> b.title("Book 3"));
        saveLibraryBook(lb -> lb.user(user).book(book1));

        service.bulkAdd(List.of(book1.getId(), book2.getId(), book3.getId()), user.getId());

        var libraryBooks = repository.findAllByUserId(user.getId());
        assertThat(libraryBooks).hasSize(3);
        var bookIds = libraryBooks.stream().map(lb -> lb.getBook().getId()).toList();
        assertThat(bookIds).containsExactlyInAnyOrder(book1.getId(), book2.getId(), book3.getId());
    }

    @Test
    void shouldBulkUpdateStatus() {
        var user = saveUser();
        var lb1 = saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("B1"))).status(TO_READ));
        var lb2 = saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("B2"))).status(READING));

        service.bulkUpdateStatus(List.of(lb1.getId(), lb2.getId()), user.getId(), READ);

        var updatedLb1 = testDbClient.findLibraryBookById(lb1.getId());
        var updatedLb2 = testDbClient.findLibraryBookById(lb2.getId());
        assertThat(updatedLb1).isNotNull();
        assertThat(updatedLb2).isNotNull();
        assertThat(updatedLb1.getStatus()).isEqualTo(READ);
        assertThat(updatedLb1.getFinishedAt()).isEqualTo(LocalDate.now());
        assertThat(updatedLb2.getStatus()).isEqualTo(READ);
        assertThat(updatedLb2.getFinishedAt()).isEqualTo(LocalDate.now());

        service.bulkUpdateStatus(List.of(lb1.getId()), user.getId(), READING);
        updatedLb1 = testDbClient.findLibraryBookById(lb1.getId());
        assertThat(updatedLb1).isNotNull();
        assertThat(updatedLb1.getStatus()).isEqualTo(READING);
        assertThat(updatedLb1.getFinishedAt()).isNull();
    }

    @Test
    void shouldBulkDeleteBooks() {
        var user = saveUser();
        var lb1 = saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("B1"))));
        var lb2 = saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("B2"))));

        service.bulkDelete(List.of(lb1.getId(), lb2.getId()), user.getId());

        assertThat(testDbClient.findLibraryBookById(lb1.getId())).isNull();
        assertThat(testDbClient.findLibraryBookById(lb2.getId())).isNull();
    }

    @Test
    void shouldSearchByMoodWithoutStatusFilter() {
        var user = saveUser();
        float[] v1 = new float[384];
        v1[0] = 0.9f;
        saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("Space Adventure").embedding(v1))).status(READ));

        float[] v2 = new float[384];
        v2[0] = 0.8f;
        saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("Galactic Journey").embedding(v2))).status(TO_READ));

        float[] v3 = new float[384];
        v3[1] = 0.9f;
        saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("Historical Romance").embedding(v3))).status(TO_READ));

        float[] v4 = new float[384];
        v4[1] = 0.8f;
        saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("Medieval Love").embedding(v4))).status(READING));

        float[] v5 = new float[384];
        v5[5] = 0.9f;
        saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("Cooking Basics").embedding(v5))).status(TO_READ));

        var results = service.searchByMood("space trip", null, user.getId(), 2);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getBook().getTitle()).containsAnyOf("Space Adventure", "Galactic Journey");
        assertThat(results.get(1).getBook().getTitle()).containsAnyOf("Space Adventure", "Galactic Journey");
    }

    @Test
    void shouldSearchByMoodWithStatusFilter() {
        var user = saveUser();
        float[] v1 = new float[384];
        v1[0] = 0.9f;
        saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("Space Adventure").embedding(v1))).status(READ));

        float[] v2 = new float[384];
        v2[0] = 0.8f;
        saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("Galactic Journey").embedding(v2))).status(TO_READ));

        float[] v3 = new float[384];
        v3[1] = 0.9f;
        saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("Historical Romance").embedding(v3))).status(TO_READ));

        float[] v4 = new float[384];
        v4[1] = 0.8f;
        saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("Medieval Love").embedding(v4))).status(READING));

        float[] v5 = new float[384];
        v5[5] = 0.9f;
        saveLibraryBook(lb -> lb.user(user).book(saveBook(b -> b.title("Cooking Basics").embedding(v5))).status(TO_READ));

        var results = service.searchByMood("space trip", TO_READ, user.getId(), 2);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getBook().getTitle()).isEqualTo("Galactic Journey");
        assertThat(results.stream().anyMatch(r -> r.getBook().getTitle().equals("Space Adventure"))).isFalse();
    }

    @Test
    void shouldCascadeDeleteDependencies() {
        var user = saveUser();
        var book = saveBook(b -> b.title("Cascade Test Book"));
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
        var collection = saveCollection(c -> c.user(user).name("Test Collection"));
        var collectionBook = saveCollectionBook(cb -> cb.collection(collection).libraryBook(libraryBook));
        var note = saveNote(n -> n.libraryBook(libraryBook).content("Test Note").noteType(TEXT));
        var quote = saveQuote(q -> q.libraryBook(libraryBook).text("Test Quote 1"));

        service.delete(libraryBook.getId(), user.getId());

        assertThat(testDbClient.findLibraryBookById(libraryBook.getId())).isNull();
        assertThat(testDbClient.countNotes()).isZero();
        assertThat(testDbClient.countQuotes()).isZero();
        assertThat(testDbClient.findCollectionBookById(collection.getId(), libraryBook.getId())).isNull();
    }

}

package org.example.library.note.service;

import org.example.library.book.domain.Book;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.note.domain.Note;
import org.example.library.note.dto.NoteRequest;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;
import static org.example.library.note.domain.Note.NoteType.TEXT;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class NoteServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private NoteService service;

    @BeforeEach
    void cleanDbBefore() {
        testDbClient.cleanDatabase();
    }

    @AfterEach
    void tearDownEach() {
        testDbClient.cleanDatabase();
    }

    @Test
    void shouldGetNoteByLibraryBookId() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(user, book);
        var note = Note.builder()
                .content("Sample Note")
                .noteType(TEXT)
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveNote(note);

        var result = service.getByLibraryBookId(libraryBook.getId(), user.getId());

        assertThat(result.content()).isEqualTo("Sample Note");
    }

    @Test
    void shouldThrowNotFoundWhenNoteDoesNotExist() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(user, book);

        assertThatThrownBy(() -> service.getByLibraryBookId(libraryBook.getId(), user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.note.not_found");
    }

    @Test
    void shouldCreateNewNote() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(user, book);
        var request = new NoteRequest(libraryBook.getId(), "New Note Content");

        var result = service.createOrUpdate(request, user.getId());

        assertThat(result.id()).isNotNull();
        assertThat(result.content()).isEqualTo("New Note Content");
        var savedNote = testDbClient.findNoteById(result.id());
        assertThat(savedNote).isNotNull();
        assertThat(savedNote.getContent()).isEqualTo("New Note Content");
    }

    @Test
    void shouldUpdateExistingNote() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(user, book);
        var note = Note.builder()
                .content("Old Content")
                .noteType(TEXT)
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveNote(note);
        var request = new NoteRequest(libraryBook.getId(), "Updated Content");

        var result = service.createOrUpdate(request, user.getId());

        assertThat(result.id()).isEqualTo(note.getId());
        assertThat(result.content()).isEqualTo("Updated Content");
        var updatedNote = testDbClient.findNoteById(note.getId());
        assertThat(updatedNote).isNotNull();
        assertThat(updatedNote.getContent()).isEqualTo("Updated Content");
    }

    @Test
    void shouldDeleteNote() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(user, book);
        var note = Note.builder()
                .content("Note to delete")
                .noteType(TEXT)
                .libraryBook(libraryBook)
                .build();
        testDbClient.saveNote(note);

        service.delete(libraryBook.getId(), user.getId());

        assertThat(testDbClient.findNoteById(note.getId())).isNull();
    }

    private User saveUser() {
        var user = User.builder()
                .email("user@example.com")
                .fullName("User")
                .password("pass")
                .role(USER)
                .build();

        testDbClient.saveUser(user);
        return user;
    }

    private Book saveBook() {
        var book = Book.builder()
                .status(NEW)
                .popularityCount(0)
                .build();

        testDbClient.saveBook(book);
        return book;
    }

    private LibraryBook saveLibraryBook(User user, Book book) {
        var libraryBook = LibraryBook.builder()
                .user(user)
                .book(book)
                .title("Title")
                .status(NO_TAG)
                .build();

        testDbClient.saveLibraryBook(libraryBook);
        return libraryBook;
    }

}

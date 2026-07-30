package org.example.library.note.service;

import org.example.library.common.exception.NotFoundException;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.example.library.note.dto.NoteRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.note.domain.Note.NoteType.TEXT;

class NoteServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private NoteService service;

    @Test
    void shouldGetNoteByLibraryBookId() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
        saveNote(n -> n.libraryBook(libraryBook).content("Sample Note").noteType(TEXT));

        var result = service.getByLibraryBookId(libraryBook.getId(), user.getId());

        assertThat(result.content()).isEqualTo("Sample Note");
    }

    @Test
    void shouldThrowNotFoundWhenNoteDoesNotExist() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));

        assertThatThrownBy(() -> service.getByLibraryBookId(libraryBook.getId(), user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.note.not_found");
    }

    @Test
    void shouldCreateNewNote() {
        var user = saveUser();
        var book = saveBook();
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
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
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
        var note = saveNote(n -> n.libraryBook(libraryBook).content("Old Content").noteType(TEXT));
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
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
        var note = saveNote(n -> n.libraryBook(libraryBook).content("Note to delete").noteType(TEXT));

        service.delete(libraryBook.getId(), user.getId());

        assertThat(testDbClient.findNoteById(note.getId())).isNull();
    }

}

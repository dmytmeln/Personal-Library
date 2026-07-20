package org.example.library.note.repository;

import org.example.library.book.domain.Book;
import org.example.library.config.AbstractRepositoryTest;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.note.domain.Note;
import org.example.library.user.domain.User;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.config.EntityRecursiveComparisonConfigs.LIBRARY_BOOK_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.NOTE_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.NOTE_SAVED;
import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;
import static org.example.library.note.domain.Note.NoteType.TEXT;
import static org.example.library.user.domain.Role.USER;

class NoteRepositoryTest extends AbstractRepositoryTest<NoteRepository> {

    @Test
    void save_ShouldPersistNote_AndNotCascadeLibraryBookOrUser() {
        User user = createUser();
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        long initialUserCount = testDbClient.countUsers();
        long initialLibraryBookCount = testDbClient.countLibraryBooks();
        Note expected = createNote(libraryBook);

        Note actual = repository.save(expected);

        assertThat(actual)
                .usingRecursiveComparison(NOTE_SAVED)
                .isEqualTo(expected);
        Note dbState = testDbClient.findNoteById(actual.getId());
        assertThat(dbState)
                .isNotNull()
                .usingRecursiveComparison(NOTE_DIRECT_FIELDS)
                .isEqualTo(actual);
        assertThat(testDbClient.countUsers()).isEqualTo(initialUserCount);
        assertThat(testDbClient.countLibraryBooks()).isEqualTo(initialLibraryBookCount);
    }

    @Test
    @Transactional
    void findById_ShouldReturnNote_WhenExists() {
        User user = createUser();
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Note note = createNote(libraryBook);
        testDbClient.saveNote(note);

        Optional<Note> actual = repository.findById(note.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(NOTE_DIRECT_FIELDS)
                .isEqualTo(note);
        assertThat(actual.get().getLibraryBook())
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(LIBRARY_BOOK_DIRECT_FIELDS)
                .isEqualTo(libraryBook);
    }

    @Test
    void delete_ShouldRemoveNote_ButKeepLibraryBook() {
        User user = createUser();
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Note note = createNote(libraryBook);
        testDbClient.saveNote(note);
        long initialLibraryBookCount = testDbClient.countLibraryBooks();

        repository.delete(note);

        assertThat(testDbClient.findNoteById(note.getId())).isNull();
        assertThat(testDbClient.countLibraryBooks()).isEqualTo(initialLibraryBookCount);
    }

    @Test
    void findByLibraryBookIdAndLibraryBookUserId_ShouldReturnNote_WhenExists() {
        User user = createUser();
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Note note = createNote(libraryBook);
        testDbClient.saveNote(note);

        Optional<Note> actual = repository.findByLibraryBookIdAndLibraryBookUserId(libraryBook.getId(), user.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(note.getId());
    }

    @Test
    void findByLibraryBookIdAndLibraryBookUserId_ShouldReturnEmpty_WhenDoesNotExist() {
        Optional<Note> actual = repository.findByLibraryBookIdAndLibraryBookUserId(999, 999);

        assertThat(actual).isEmpty();
    }

    @Test
    void deleteByLibraryBookIdAndLibraryBookUserId_ShouldDeleteNote() {
        User user = createUser();
        testDbClient.saveUser(user);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, user);
        testDbClient.saveLibraryBook(libraryBook);
        Note note = createNote(libraryBook);
        testDbClient.saveNote(note);

        transactionTemplate.executeWithoutResult(
                status -> repository.deleteByLibraryBookIdAndLibraryBookUserId(libraryBook.getId(), user.getId()));

        assertThat(testDbClient.findNoteById(note.getId())).isNull();
    }

    private User createUser() {
        return User.builder()
                .email("note-owner@example.com")
                .fullName("Note Owner")
                .password("password")
                .role(USER)
                .build();
    }

    private Book createBook() {
        return Book.builder()
                .publishYear((short) 2010)
                .pages((short) 250)
                .coverImageUrl("http://example.com/cover.png")
                .popularityCount(0)
                .build();
    }

    private LibraryBook createLibraryBook(Book book, User user) {
        return LibraryBook.builder()
                .book(book)
                .user(user)
                .status(NO_TAG)
                .build();
    }

    private Note createNote(LibraryBook libraryBook) {
        return Note.builder()
                .libraryBook(libraryBook)
                .content("Test Content")
                .noteType(TEXT)
                .rawTranscript("Raw transcript text")
                .transcriptionModel("whisper-1")
                .formattingModel("gpt-4")
                .voiceCreatedAt(LocalDateTime.of(2023, JUNE, 15, 12, 0))
                .build();
    }

}

package org.example.library.config;

import org.example.library.library_book.domain.LibraryBook;
import org.example.library.note.domain.Note;

import java.time.LocalDateTime;

import static org.example.library.note.domain.Note.NoteType.TEXT;

public class NoteConfigurer {

    private final TestDbClient testDbClient;

    private LibraryBook libraryBook;
    private boolean libraryBookSet;
    private String content = "Test note content";
    private Note.NoteType noteType = TEXT;
    private String rawTranscript;
    private String transcriptionModel;
    private String formattingModel;
    private LocalDateTime voiceCreatedAt;

    public NoteConfigurer(TestDbClient testDbClient) {
        this.testDbClient = testDbClient;
    }

    public NoteConfigurer libraryBook(LibraryBook libraryBook) {
        this.libraryBook = libraryBook;
        this.libraryBookSet = true;
        return this;
    }

    public NoteConfigurer content(String content) {
        this.content = content;
        return this;
    }

    public NoteConfigurer noteType(Note.NoteType noteType) {
        this.noteType = noteType;
        return this;
    }

    public NoteConfigurer rawTranscript(String rawTranscript) {
        this.rawTranscript = rawTranscript;
        return this;
    }

    public NoteConfigurer transcriptionModel(String transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
        return this;
    }

    public NoteConfigurer formattingModel(String formattingModel) {
        this.formattingModel = formattingModel;
        return this;
    }

    public NoteConfigurer voiceCreatedAt(LocalDateTime voiceCreatedAt) {
        this.voiceCreatedAt = voiceCreatedAt;
        return this;
    }

    public Note save() {
        if (!libraryBookSet) {
            libraryBook = new LibraryBookConfigurer(testDbClient).save();
        }

        var note = Note.builder()
                .libraryBook(libraryBook)
                .content(content)
                .noteType(noteType)
                .rawTranscript(rawTranscript)
                .transcriptionModel(transcriptionModel)
                .formattingModel(formattingModel)
                .voiceCreatedAt(voiceCreatedAt)
                .build();

        testDbClient.saveNote(note);
        return note;
    }

}

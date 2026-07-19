package org.example.library.note.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.library.library_book.domain.LibraryBook;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static org.example.library.note.domain.Note.NoteType.TEXT;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "notes_seq")
    @SequenceGenerator(name = "notes_seq", sequenceName = "notes_seq", allocationSize = 20)
    @Column(name = "note_id")
    private Integer id;

    @Column(name = "content", nullable = true, columnDefinition = "text")
    private String content;

    @Enumerated(STRING)
    @Column(name = "note_type", nullable = false, length = 20)
    @Builder.Default
    private NoteType noteType = TEXT;

    @Column(name = "raw_transcript", nullable = true, columnDefinition = "text")
    private String rawTranscript;

    @Column(name = "transcription_model", nullable = true)
    private String transcriptionModel;

    @Column(name = "formatting_model", nullable = true)
    private String formattingModel;

    @Column(name = "voice_created_at", nullable = true)
    private LocalDateTime voiceCreatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "library_book_id", nullable = false, unique = true)
    @OnDelete(action = CASCADE)
    private LibraryBook libraryBook;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Note note)) {
            return false;
        }

        return Objects.equals(this.id, note.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    public enum NoteType {
        TEXT,
        VOICE
    }

}

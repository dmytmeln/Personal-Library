package org.example.library.note.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.library.library_book.domain.LibraryBook;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notes_seq")
    @SequenceGenerator(name = "notes_seq", sequenceName = "notes_seq", allocationSize = 20)
    @Column(name = "note_id")
    private Integer id;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_book_id", nullable = false, unique = true)
    private LibraryBook libraryBook;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Note note)) return false;
        return Objects.equals(id, note.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

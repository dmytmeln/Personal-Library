package org.example.library.userBooks.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.library.book.domain.Book;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_books") // todo: change to library_books
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBook {

    @EmbeddedId
    private UserBookId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserBookStatus status;

    @Column(name = "date_added", nullable = false)
    @CreationTimestamp
    private LocalDateTime dateAdded;

    @Column(name = "rating")
    private Byte rating;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("bookId")
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserBook userBook)) return false;
        return Objects.equals(id, userBook.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

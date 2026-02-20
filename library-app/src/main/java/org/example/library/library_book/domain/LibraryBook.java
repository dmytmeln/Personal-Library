package org.example.library.library_book.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.library.book.domain.Book;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.user.domain.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "library_books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "library_book_id")
    private Integer id; // todo embedded ID

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private LibraryBookStatus status = LibraryBookStatus.NO_TAG;

    @Column(name = "added_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime addedAt;

    @Column(name = "rating")
    private Byte rating;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static LibraryBook of(Book book, User user) {
        return LibraryBook.builder()
                .book(book)
                .user(user)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LibraryBook libraryBook)) return false;
        return Objects.equals(id, libraryBook.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

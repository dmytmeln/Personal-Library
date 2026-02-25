package org.example.library.library_book.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.library.book.domain.Book;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.user.domain.User;
import org.example.library.note.domain.Note;
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

    @Column(name = "title")
    private String title;

    @Column(name = "publish_year")
    private Short publishYear;

    @Column(name = "pages")
    private Short pages;

    @Column(name = "language")
    private String language;

    @Column(name = "description")
    private String description;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(mappedBy = "libraryBook", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Note note;

    public static LibraryBook of(Book book, User user) {
        return LibraryBook.builder()
                .book(book)
                .user(user)
                .build();
    }

    public void resetOverriddenFields() {
        this.title = null;
        this.publishYear = null;
        this.pages = null;
        this.language = null;
        this.description = null;
        this.coverImageUrl = null;
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

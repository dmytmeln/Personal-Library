package org.example.library.library_book.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.library.book.domain.Book;
import org.example.library.user.domain.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;

@Entity
@Table(name = "library_books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryBook {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "library_books_seq")
    @SequenceGenerator(name = "library_books_seq", sequenceName = "library_books_seq", allocationSize = 20)
    @Column(name = "library_book_id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private LibraryBookStatus status = NO_TAG;

    @Column(name = "added_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime addedAt;

    @Column(name = "finished_at")
    private LocalDate finishedAt;

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

    @Column(name = "location")
    private String location;

    @Column(name = "custom_author_name")
    private String customAuthorName;

    @Column(name = "custom_category_name")
    private String customCategoryName;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = LAZY, optional = false)
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
        if (!(o instanceof LibraryBook libraryBook)) {
            return false;
        }

        return Objects.equals(this.id, libraryBook.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

}

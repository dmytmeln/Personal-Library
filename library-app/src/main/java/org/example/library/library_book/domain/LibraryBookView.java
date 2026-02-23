package org.example.library.library_book.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.library.author.domain.Author;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "library_books_view")
@Immutable
@Getter
public class LibraryBookView {

    @Id
    @Column(name = "library_book_id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "book_id")
    private Integer bookId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LibraryBookStatus status;

    @Column(name = "added_at")
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

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @ManyToMany
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id", referencedColumnName = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors;

}

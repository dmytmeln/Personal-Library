package org.example.library.book.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.library.author.domain.Author;
import org.hibernate.annotations.Immutable;

import java.util.Set;

@Entity
@Table(name = "books_display_view")
@Immutable
@Getter
public class BookDisplayView {

    @Id
    @Column(name = "book_id")
    private Integer id;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "publish_year")
    private Short publishYear;

    @Column(name = "pages")
    private Short pages;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "popularity_count")
    private Integer popularityCount;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "book_language")
    private String bookLanguage;

    @Column(name = "category_name")
    private String categoryName;

    @ManyToMany
    @JoinTable(
            name = "book_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors;

}

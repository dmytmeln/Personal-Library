package org.example.library.book.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.library.author.domain.Author;
import org.example.library.category.domain.Category;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "books")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "publish_year", nullable = false)
    private Short publishYear;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "pages", nullable = false)
    private Short pages;

    @Column(name = "description")
    private String description;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @ManyToMany
    @JoinTable(
            name = "book_authors",
            joinColumns = {@JoinColumn(name = "book_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "author_id", nullable = false)}
    )
    private List<Author> authors;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Book book)) return false;
        return Objects.equals(id, book.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

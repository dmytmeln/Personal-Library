package org.example.library.book.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.library.author.domain.Author;
import org.example.library.category.domain.Category;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "books")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "books_seq")
    @SequenceGenerator(name = "books_seq", sequenceName = "books_seq", allocationSize = 20)
    @Column(name = "book_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "publish_year", nullable = false)
    private Short publishYear;

    @Column(name = "pages", nullable = false)
    private Short pages;

    @Column(name = "cover_image_url", nullable = false)
    private String coverImageUrl;

    @Column(name = "description_vector", columnDefinition = "vector(1100)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1100)
    private float[] descriptionVector;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BookStatus status = BookStatus.NEW;

    @Column(name = "vector_version")
    private Integer vectorVersion;

    @Column(name = "popularity_count", nullable = false)
    private Integer popularityCount;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @MapKey(name = "languageCode")
    private Map<String, BookTranslation> translations;

    @ManyToMany
    @JoinTable(
            name = "book_authors",
            joinColumns = {@JoinColumn(name = "book_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "author_id", nullable = false)}
    )
    private Set<Author> authors;

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

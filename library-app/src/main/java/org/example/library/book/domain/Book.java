package org.example.library.book.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.library.author.domain.Author;
import org.example.library.category.domain.Category;
import org.example.library.user.domain.User;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.hibernate.type.SqlTypes.VECTOR;

@Entity
@Table(name = "books")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "books_seq")
    @SequenceGenerator(name = "books_seq", sequenceName = "books_seq", allocationSize = 20, initialValue = 56)
    @Column(name = "book_id")
    private Integer id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "publish_year")
    private Short publishYear;

    @Column(name = "pages")
    private Short pages;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "embedding", columnDefinition = "vector(384)")
    @JdbcTypeCode(VECTOR)
    @Array(length = 384)
    private float[] embedding;

    @Enumerated(STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BookStatus status = NEW;

    @Column(name = "popularity_count", nullable = false)
    private Integer popularityCount;

    @OneToMany(mappedBy = "book", cascade = ALL, fetch = LAZY)
    @MapKey(name = "languageCode")
    @Builder.Default
    private Map<String, BookTranslation> translations = new HashMap<>();

    @ManyToMany
    @JoinTable(name = "book_authors",
            joinColumns = {@JoinColumn(name = "book_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "author_id", nullable = false)})
    @Builder.Default
    private Set<Author> authors = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Book book)) {
            return false;
        }

        return Objects.equals(this.id, book.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    public BookTranslation getTranslation(String languageCode) {
        Objects.requireNonNull(this.translations, "Book translations must not be null");

        return this.translations.get(languageCode);
    }

}

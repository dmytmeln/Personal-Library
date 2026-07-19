package org.example.library.author.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.Book_;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "authors_seq")
    @SequenceGenerator(name = "authors_seq", sequenceName = "authors_seq", allocationSize = 20, initialValue = 20)
    @Column(name = "author_id")
    private Integer id;

    @Column(name = "birth_year", nullable = false)
    private Short birthYear;

    @Column(name = "death_year")
    private Short deathYear;

    @Column(name = "popularity_count", nullable = false)
    @Builder.Default
    private Integer popularityCount = 0;

    @OneToMany(mappedBy = "author", cascade = ALL, fetch = LAZY)
    @MapKey(name = "languageCode")
    @Builder.Default
    private Map<String, AuthorTranslation> translations = new HashMap<>();

    @ManyToMany(mappedBy = Book_.AUTHORS)
    private List<Book> books;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Author author)) {
            return false;
        }

        return Objects.equals(this.id, author.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    public AuthorTranslation getTranslation(String languageCode) {
        if (this.translations == null) {
            throw new NullPointerException("Author translations must not be null");
        }

        return this.translations.get(languageCode);
    }

}

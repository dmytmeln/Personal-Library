package org.example.library.category.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
@Table(name = "categories")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "categories_seq")
    @SequenceGenerator(name = "categories_seq", sequenceName = "categories_seq", allocationSize = 20, initialValue = 11)
    @Column(name = "category_id")
    private Integer id;

    @Column(name = "popularity_count", nullable = false)
    private Integer popularityCount;

    @OneToMany(mappedBy = "category", cascade = ALL, fetch = LAZY)
    @MapKey(name = "languageCode")
    @Builder.Default
    private Map<String, CategoryTranslation> translations = new HashMap<>();

    @OneToMany(mappedBy = Book_.CATEGORY)
    private List<Book> books;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Category category)) {
            return false;
        }

        return Objects.equals(this.id, category.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    public CategoryTranslation getTranslation(String languageCode) {
        if (this.translations == null) {
            throw new NullPointerException("Category translations must not be null");
        }

        return this.translations.get(languageCode);
    }

}

package org.example.library.author.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.Book_;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "authors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Integer id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "birth_year", nullable = false)
    private Short birthYear;

    @Column(name = "death_year", nullable = false)
    private Short deathYear;

    @Column(name = "biography")
    private String biography;

    @ManyToMany(mappedBy = Book_.AUTHORS)
    private List<Book> books;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Author author)) return false;
        return Objects.equals(id, author.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

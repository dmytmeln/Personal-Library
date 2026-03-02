package org.example.library.author.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "authors_display_view")
@Immutable
@Getter
public class AuthorDisplayView {

    @Id
    @Column(name = "author_id")
    private Integer id;

    @Column(name = "birth_year")
    private Short birthYear;

    @Column(name = "death_year")
    private Short deathYear;

    @Column(name = "popularity_count")
    private Integer popularityCount;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "country")
    private String country;

    @Column(name = "biography")
    private String biography;

}

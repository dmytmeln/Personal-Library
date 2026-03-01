package org.example.library.author.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "author_translations")
@IdClass(AuthorTranslationId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorTranslation {

    @Id
    @Column(name = "author_id")
    private Integer authorId;

    @Id
    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "biography")
    private String biography;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private Author author;

}

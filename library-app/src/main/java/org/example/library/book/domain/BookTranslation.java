package org.example.library.book.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_translations")
@IdClass(BookTranslationId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookTranslation {

    @Id
    @Column(name = "book_id")
    private Integer bookId;

    @Id
    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "book_language", nullable = false)
    private String bookLanguage;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    private Book book;

}

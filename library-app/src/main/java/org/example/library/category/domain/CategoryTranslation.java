package org.example.library.category.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category_translations")
@IdClass(CategoryTranslationId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTranslation {

    @Id
    @Column(name = "category_id")
    private Integer categoryId;

    @Id
    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    @MapsId("categoryId")
    private Category category;

}

package org.example.library.category.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "categories_display_view")
@Immutable
@Getter
public class CategoryDisplayView {

    @Id
    @Column(name = "category_id")
    private Integer id;

    @Column(name = "popularity_count")
    private Integer popularityCount;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

}

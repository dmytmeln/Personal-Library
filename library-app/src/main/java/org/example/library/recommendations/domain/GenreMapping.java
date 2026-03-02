package org.example.library.recommendations.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

@Entity
@Table(name = "genre_mapping")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenreMapping implements Persistable<Integer> {

    @Id
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "vector_index", nullable = false, unique = true)
    private Integer vectorIndex;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Nullable
    @Override
    public Integer getId() {
        return this.categoryId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

}

package org.example.library.collection_book.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.library.collection.domain.Collection;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.domain.LibraryBookView;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(name = "collection_books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionBook implements Persistable<CollectionBookId> {

    @EmbeddedId
    private CollectionBookId id;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @ManyToOne(fetch = LAZY, optional = false)
    @MapsId("libraryBookId")
    @JoinColumn(name = "library_book_id", nullable = false)
    @OnDelete(action = CASCADE)
    private LibraryBook libraryBook;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "library_book_id", insertable = false, updatable = false)
    private LibraryBookView libraryBookView;

    @ManyToOne(fetch = LAZY, optional = false)
    @MapsId("collectionId")
    @JoinColumn(name = "collection_id", nullable = false)
    @OnDelete(action = CASCADE)
    private Collection collection;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CollectionBook collectionBook)) {
            return false;
        }

        return Objects.equals(this.id, collectionBook.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

}

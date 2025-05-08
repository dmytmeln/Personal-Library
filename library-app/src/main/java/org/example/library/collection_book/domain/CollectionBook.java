package org.example.library.collection_book.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.library.collection.domain.Collection;
import org.example.library.library_book.domain.LibraryBook;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "collection_books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionBook {

    @EmbeddedId
    private CollectionBookId id;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("libraryBookId")
    @JoinColumn(name = "library_book_id", nullable = false)
    private LibraryBook libraryBook;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("collectionId")
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CollectionBook collectionBook)) return false;
        return Objects.equals(id, collectionBook.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

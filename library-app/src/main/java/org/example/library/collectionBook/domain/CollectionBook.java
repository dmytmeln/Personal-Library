package org.example.library.collectionBook.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.library.book.domain.Book;
import org.example.library.collection.domain.Collection;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "collections_book") // todo: change table name
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionBook {

    @EmbeddedId
    private CollectionBookId id;

    @CreationTimestamp
    @Column(name = "date_added", nullable = false)
    private LocalDateTime dateAdded;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("bookId")
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

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

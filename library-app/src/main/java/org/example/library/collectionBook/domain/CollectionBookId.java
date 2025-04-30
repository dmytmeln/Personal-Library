package org.example.library.collectionBook.domain;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record CollectionBookId(
        Integer collectionId,
        Integer bookId
) {

    public CollectionBookId() {
        this(null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CollectionBookId collectionBookId)) return false;
        return Objects.equals(collectionId, collectionBookId.collectionId())
                && Objects.equals(bookId, collectionBookId.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionId, bookId);
    }

}

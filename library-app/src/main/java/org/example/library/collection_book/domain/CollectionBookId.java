package org.example.library.collection_book.domain;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record CollectionBookId(
        Integer collectionId,
        Integer libraryBookId
) {

    public CollectionBookId() {
        this(null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CollectionBookId collectionBookId)) return false;
        return Objects.equals(collectionId, collectionBookId.collectionId())
                && Objects.equals(libraryBookId, collectionBookId.libraryBookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionId, libraryBookId);
    }

}

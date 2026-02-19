package org.example.library.collection_book.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class CollectionBookId {

    private final Integer collectionId;
    private final Integer libraryBookId;


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CollectionBookId collectionBookId)) return false;
        return Objects.equals(collectionId, collectionBookId.getCollectionId())
                && Objects.equals(libraryBookId, collectionBookId.getLibraryBookId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionId, libraryBookId);
    }

}

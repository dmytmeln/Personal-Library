package org.example.library.collection.dto;

public record CollectionTreeProjection(
        Integer id,
        String name,
        Integer parentId
) {
}

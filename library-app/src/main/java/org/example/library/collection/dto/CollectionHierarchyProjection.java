package org.example.library.collection.dto;

public record CollectionHierarchyProjection(
        Integer id,
        String name,
        Integer parentId
) {
}

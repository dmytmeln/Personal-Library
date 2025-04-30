package org.example.library.collection.dto;

public record CollectionDto(
        Integer id,
        String name,
        String description,
        String color,
        String updatedAt
) {
}

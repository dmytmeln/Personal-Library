package org.example.library.author.dto;

public record AuthorDto(
        Integer id,
        String fullName,
        String country,
        Short birthYear,
        Short deathYear,
        String biography
) {
}

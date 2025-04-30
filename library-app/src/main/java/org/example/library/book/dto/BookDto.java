package org.example.library.book.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record BookDto(
        Integer id,
        String title,
        Integer categoryId,
        String categoryName,
        Short publishYear,
        String language,
        Short pages,
        String description,
        String coverImageUrl,
        Map<Integer, String> authors
) {}

package org.example.library.library_book.dto;

import lombok.Builder;

@Builder
public record UpdateLibraryBookDetailsDto(
        String title,
        Short publishYear,
        Short pages,
        String language,
        String description,
        String coverImageUrl
) {
}

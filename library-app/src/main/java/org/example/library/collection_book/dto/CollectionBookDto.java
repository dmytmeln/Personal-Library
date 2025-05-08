package org.example.library.collection_book.dto;

import org.example.library.library_book.dto.LibraryBookDto;

import java.time.LocalDateTime;

public record CollectionBookDto(
        LibraryBookDto libraryBook,
        LocalDateTime addedAt
) {
}

package org.example.library.collection.dto;

import org.example.library.book.dto.BookDto;

import java.time.LocalDateTime;

public record CollectionBookDto(
        BookDto book,
        LocalDateTime dateAdded
) {
}

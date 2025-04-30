package org.example.library.collectionBook.dto;

import org.example.library.book.dto.BookDto;

import java.time.LocalDateTime;

public record CollectionBookDto(
        BookDto book,
        LocalDateTime dateAdded
) {
}

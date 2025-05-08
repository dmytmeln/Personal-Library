package org.example.library.library_book.dto;

import lombok.Builder;
import org.example.library.book.dto.BookDto;

import java.time.LocalDateTime;

@Builder
public record LibraryBookDto(
        Integer id,
        String status,
        LocalDateTime addedAt,
        Byte rating,
        BookDto book
) {
}

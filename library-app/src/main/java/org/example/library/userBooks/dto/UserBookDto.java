package org.example.library.userBooks.dto;

import lombok.Builder;
import org.example.library.book.dto.BookDto;

import java.time.LocalDateTime;

@Builder
public record UserBookDto(
        String status,
        LocalDateTime dateAdded,
        Byte rating,
        BookDto book
) {}

package org.example.library.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NoteRequest(
    @NotNull(message = "{validation.note.library_book_id.required}") @Positive Integer libraryBookId,
    @NotBlank(message = "{validation.note.content.required}") String content
) {
}


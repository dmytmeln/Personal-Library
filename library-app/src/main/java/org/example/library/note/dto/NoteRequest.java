package org.example.library.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NoteRequest(
    @NotNull @Positive Integer libraryBookId,
    @NotBlank String content
) {}

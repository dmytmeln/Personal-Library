package org.example.library.note.dto;

import java.time.LocalDateTime;

public record NoteDto(
    Integer id,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

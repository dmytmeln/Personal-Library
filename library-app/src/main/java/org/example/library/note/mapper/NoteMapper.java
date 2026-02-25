package org.example.library.note.mapper;

import org.example.library.note.domain.Note;
import org.example.library.note.dto.NoteDto;
import org.example.library.note.dto.NoteRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    NoteDto toDto(Note note);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "libraryBook", ignore = true)
    Note toEntity(NoteRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "libraryBook", ignore = true)
    void update(@MappingTarget Note note, NoteRequest request);
}

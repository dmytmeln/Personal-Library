package org.example.library.author.mapper;

import org.example.library.author.domain.Author;
import org.example.library.author.dto.AuthorDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    AuthorDto toDto(Author author);

    List<AuthorDto> toDto(List<Author> authors);

}

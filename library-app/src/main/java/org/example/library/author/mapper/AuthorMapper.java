package org.example.library.author.mapper;

import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorDisplayView;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.author.dto.AuthorDto;
import org.example.library.author.dto.AuthorResponse;
import org.example.library.author.dto.AuthorResponse.AuthorTranslationResponse;
import org.example.library.author.dto.AuthorSaveRequest;
import org.example.library.common.localization.DefaultLanguage;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface AuthorMapper {

    AuthorDto toDto(AuthorDisplayView authorDisplayView);

    @Mapping(target = "id", source = "author.id")
    @Mapping(target = "birthYear", source = "author.birthYear")
    @Mapping(target = "deathYear", source = "author.deathYear")
    @Mapping(target = "fullName", source = "translation.fullName")
    @Mapping(target = "country", source = "translation.country")
    @Mapping(target = "biography", source = "translation.biography")
    AuthorDto toDto(Author author, AuthorTranslation translation);

    List<AuthorDto> toDto(List<Author> authors, @Context String languageCode, @Context DefaultLanguage defaultLanguage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "popularityCount", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "translations", ignore = true)
    Author toEntity(AuthorSaveRequest request);

    AuthorResponse toAuthorResponse(Author author);

    AuthorTranslationResponse toTranslationResponse(AuthorTranslation translation);

    default AuthorDto toDto(Author author, @Context String languageCode, @Context DefaultLanguage defaultLanguage) {
        if (author == null) {
            return null;
        }

        var translation = author.getTranslationOrDefault(languageCode, defaultLanguage.code());
        return toDto(author, translation);
    }

    @AfterMapping
    default void linkTranslations(AuthorSaveRequest request, @MappingTarget Author author) {
        request.getTranslations().forEach((languageCode, translationDto) -> author.upsertTranslation(languageCode,
                translationDto.getFullName(),
                translationDto.getCountry(),
                translationDto.getBiography()));
    }

}

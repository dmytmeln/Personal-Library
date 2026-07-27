package org.example.library.author.mapper;

import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorDisplayView;
import org.example.library.author.dto.AuthorDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface AuthorMapper {

    AuthorDto toDto(AuthorDisplayView authorDisplayView);

    @Mapping(target = "fullName", source = "author", qualifiedByName = "getLocalizedFullName")
    @Mapping(target = "country", source = "author", qualifiedByName = "getLocalizedCountry")
    @Mapping(target = "biography", source = "author", qualifiedByName = "getLocalizedBiography")
    AuthorDto toDto(Author author, @Context String languageCode);

    List<AuthorDto> toDto(List<Author> authors, @Context String languageCode);

    @Named("getLocalizedFullName")
    default String getLocalizedFullName(Author author, @Context String languageCode) {
        var translation = author.getTranslations().get(languageCode);
        if (translation == null) {
            throw new IllegalStateException("Translation not found for author: " + author.getId());
        }

        return translation.getFullName();
    }

    @Named("getLocalizedCountry")
    default String getLocalizedCountry(Author author, @Context String languageCode) {
        var translation = author.getTranslations().get(languageCode);
        if (translation == null) {
            throw new IllegalStateException("Translation not found for author: " + author.getId());
        }

        return translation.getCountry();
    }

    @Named("getLocalizedBiography")
    default String getLocalizedBiography(Author author, @Context String languageCode) {
        var translation = author.getTranslations().get(languageCode);
        if (translation == null) {
            throw new IllegalStateException("Translation not found for author: " + author.getId());
        }

        return translation.getBiography();
    }

}

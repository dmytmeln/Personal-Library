package org.example.library.author.mapper;

import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorDisplayView;
import org.example.library.author.dto.AuthorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    @Mapping(target = "fullName", source = "author", qualifiedByName = "getLocalizedFullName")
    @Mapping(target = "country", source = "author", qualifiedByName = "getLocalizedCountry")
    @Mapping(target = "biography", source = "author", qualifiedByName = "getLocalizedBiography")
    AuthorDto toDto(Author author);

    AuthorDto toDto(AuthorDisplayView authorDisplayView);

    List<AuthorDto> toDto(List<Author> authors);

    @Named("getLocalizedFullName")
    default String getLocalizedFullName(Author author) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        var translation = author.getTranslations().get(lang);
        if (translation == null)
            throw new IllegalStateException("Translation not found for author: " + author.getId());

        return translation.getFullName();
    }

    @Named("getLocalizedCountry")
    default String getLocalizedCountry(Author author) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        var translation = author.getTranslations().get(lang);
        if (translation == null)
            throw new IllegalStateException("Translation not found for author: " + author.getId());

        return translation.getCountry();
    }

    @Named("getLocalizedBiography")
    default String getLocalizedBiography(Author author) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        var translation = author.getTranslations().get(lang);
        if (translation == null)
            throw new IllegalStateException("Translation not found for author: " + author.getId());

        return translation.getBiography();
    }

}

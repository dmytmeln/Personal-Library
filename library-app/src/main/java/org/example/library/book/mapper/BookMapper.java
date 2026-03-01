package org.example.library.book.mapper;

import org.example.library.author.domain.Author;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookDisplayView;
import org.example.library.book.dto.BookDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Mapper(componentModel = "spring")
public interface BookMapper {

    List<BookDto> toDto(List<Book> books);

    @Mapping(target = "categoryName", source = "book", qualifiedByName = "getLocalizedCategoryName")
    @Mapping(target = "categoryId", source = "book.category.id")
    @Mapping(target = "authors", source = "book.authors", qualifiedByName = "authorsToMap")
    @Mapping(target = "title", source = "book", qualifiedByName = "getLocalizedTitle")
    @Mapping(target = "description", source = "book", qualifiedByName = "getLocalizedDescription")
    @Mapping(target = "language", source = "book", qualifiedByName = "getLocalizedLanguage")
    BookDto toBookDto(Book book);

    @Mapping(target = "authors", source = "view.authors", qualifiedByName = "authorsToMap")
    @Mapping(target = "language", source = "bookLanguage")
    BookDto toBookDto(BookDisplayView view);

    @Named("getLocalizedTitle")
    default String getLocalizedTitle(Book book) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        var translation = book.getTranslations().get(lang);
        if (translation == null)
            throw new IllegalStateException("Translation not found for book: " + book.getId());

        return translation.getTitle();
    }

    @Named("getLocalizedDescription")
    default String getLocalizedDescription(Book book) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        var translation = book.getTranslations().get(lang);
        if (translation == null)
            throw new IllegalStateException("Translation not found for book: " + book.getId());

        return translation.getDescription();
    }

    @Named("getLocalizedLanguage")
    default String getLocalizedLanguage(Book book) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        var translation = book.getTranslations().get(lang);
        if (translation == null)
            throw new IllegalStateException("Translation not found for book: " + book.getId());

        return translation.getBookLanguage();
    }

    @Named("getLocalizedCategoryName")
    default String getLocalizedCategoryName(Book book) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        var translation = book.getCategory().getTranslations().get(lang);
        if (translation == null)
            throw new IllegalStateException("Translation not found for category: " + book.getCategory().getId());

        return translation.getName();
    }

    @Named("authorsToMap")
    default Map<Integer, String> authorsToMap(Collection<Author> authors) {
        if (authors == null) {
            return null;
        }
        var lang = LocaleContextHolder.getLocale().getLanguage();
        return authors.stream()
                .collect(toMap(Author::getId, a -> {
                    var translation = a.getTranslations().get(lang);
                    if (translation == null)
                        throw new IllegalStateException("Translation not found for author: " + a.getId());

                    return translation.getFullName();
                }));
    }

}

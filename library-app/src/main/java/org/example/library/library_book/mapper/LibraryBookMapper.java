package org.example.library.library_book.mapper;

import org.example.library.author.domain.Author;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.domain.LibraryBookView;
import org.example.library.library_book.dto.LibraryBookDto;
import org.example.library.library_book.dto.UpdateLibraryBookDetailsDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

@Mapper(componentModel = "spring")
public interface LibraryBookMapper {

    @Mapping(target = "book.id", source = "bookId")
    @Mapping(target = "book.title", source = "title")
    @Mapping(target = "book.categoryId", source = "categoryId")
    @Mapping(target = "book.categoryName", source = "categoryName")
    @Mapping(target = "book.publishYear", source = "publishYear")
    @Mapping(target = "book.language", source = "language")
    @Mapping(target = "book.pages", source = "pages")
    @Mapping(target = "book.description", source = "description")
    @Mapping(target = "book.coverImageUrl", source = "coverImageUrl")
    @Mapping(target = "book.authors", source = "authors", qualifiedByName = "authorsToMap")
    LibraryBookDto toDto(LibraryBookView libraryBookView);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "addedAt", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "note", ignore = true)
    void update(@MappingTarget LibraryBook libraryBook, UpdateLibraryBookDetailsDto dto);

    @Named("authorsToMap")
    default Map<Integer, String> authorsToMap(Set<Author> authors) {
        if (authors == null)
            return null;

        return authors.stream()
                .collect(toMap(Author::getId, Author::getFullName));
    }

}

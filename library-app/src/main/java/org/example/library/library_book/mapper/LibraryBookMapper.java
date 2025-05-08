package org.example.library.library_book.mapper;

import org.example.library.author.domain.Author;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.dto.LibraryBookDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Mapper(componentModel = "spring")
public interface LibraryBookMapper {

    List<LibraryBookDto> toDto(List<LibraryBook> libraryBooks);

    @Mapping(target = "book.categoryId", source = "libraryBook.book.category.id")
    @Mapping(target = "book.categoryName", source = "libraryBook.book.category.name")
    @Mapping(target = "book.authors", source = "libraryBook.book.authors", qualifiedByName = "authorsToMap")
    LibraryBookDto toDto(LibraryBook libraryBook);

    @Named("authorsToMap")
    default Map<Integer, String> authorsToMap(List<Author> authors) {
        if (authors == null) {
            return null;
        }
        return authors.stream()
                .collect(toMap(Author::getId, Author::getFullName));
    }

}

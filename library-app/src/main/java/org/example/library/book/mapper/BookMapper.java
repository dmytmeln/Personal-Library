package org.example.library.book.mapper;

import org.example.library.author.domain.Author;
import org.example.library.book.domain.Book;
import org.example.library.book.dto.BookDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Mapper(componentModel = "spring")
public interface BookMapper {

    List<BookDto> toDto(List<Book> books);

    @Mapping(target = "categoryName", source = "book.category.name")
    @Mapping(target = "categoryId", source = "book.category.id")
    @Mapping(target = "authors", source = "book.authors", qualifiedByName = "authorsToMap")
    BookDto toBookDto(Book book);

    @Named("authorsToMap")
    default Map<Integer, String> authorsToMap(List<Author> authors) {
        if (authors == null) {
            return null;
        }
        return authors.stream()
                .collect(toMap(Author::getId, Author::getFullName));
    }

}

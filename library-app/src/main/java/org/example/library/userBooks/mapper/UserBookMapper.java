package org.example.library.userBooks.mapper;

import org.example.library.author.domain.Author;
import org.example.library.userBooks.domain.UserBook;
import org.example.library.userBooks.dto.UserBookDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Mapper(componentModel = "spring")
public interface UserBookMapper {

    List<UserBookDto> toDto(List<UserBook> userBooks);

    @Mapping(target = "book.categoryId", source = "userBook.book.category.id")
    @Mapping(target = "book.categoryName", source = "userBook.book.category.name")
    @Mapping(target = "book.authors", source = "userBook.book.authors", qualifiedByName = "authorsToMap")
    UserBookDto toDto(UserBook userBook);

    @Named("authorsToMap")
    default Map<Integer, String> authorsToMap(List<Author> authors) {
        if (authors == null) {
            return null;
        }
        return authors.stream()
                .collect(toMap(Author::getId, Author::getFullName));
    }

}

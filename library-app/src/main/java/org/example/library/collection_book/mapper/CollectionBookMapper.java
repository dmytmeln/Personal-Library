package org.example.library.collection_book.mapper;

import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.dto.CollectionBookDto;
import org.example.library.library_book.mapper.LibraryBookMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LibraryBookMapper.class})
public interface CollectionBookMapper {

    @Mapping(target = "libraryBook", source = "libraryBookView")
    CollectionBookDto toDto(CollectionBook collectionBook);

    List<CollectionBookDto> toDto(List<CollectionBook> collectionBooks);

}

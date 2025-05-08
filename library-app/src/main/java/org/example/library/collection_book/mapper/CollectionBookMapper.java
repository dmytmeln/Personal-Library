package org.example.library.collection_book.mapper;

import org.example.library.book.mapper.BookMapper;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.dto.CollectionBookDto;
import org.example.library.library_book.mapper.LibraryBookMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {LibraryBookMapper.class, BookMapper.class})
public interface CollectionBookMapper {

    CollectionBookDto toDto(CollectionBook collectionBook);

    List<CollectionBookDto> toDto(List<CollectionBook> collectionBooks);

}

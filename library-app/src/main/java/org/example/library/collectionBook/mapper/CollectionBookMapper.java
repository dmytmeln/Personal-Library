package org.example.library.collectionBook.mapper;

import org.example.library.book.mapper.BookMapper;
import org.example.library.collectionBook.domain.CollectionBook;
import org.example.library.collectionBook.dto.CollectionBookDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {BookMapper.class})
public interface CollectionBookMapper {

    CollectionBookDto toDto(CollectionBook collectionBook);

    List<CollectionBookDto> toDto(List<CollectionBook> collectionBooks);

}

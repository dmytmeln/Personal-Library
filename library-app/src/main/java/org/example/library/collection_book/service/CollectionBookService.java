package org.example.library.collection_book.service;

import lombok.RequiredArgsConstructor;
import org.example.library.collection.service.CollectionService;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.example.library.collection_book.dto.CollectionBookDto;
import org.example.library.collection_book.mapper.CollectionBookMapper;
import org.example.library.collection_book.repository.CollectionBookRepository;
import org.example.library.exception.BadRequestException;
import org.example.library.library_book.service.LibraryBookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionBookService {

    private final CollectionBookRepository repository;
    private final CollectionBookMapper mapper;
    private final CollectionService collectionService;
    private final LibraryBookService libraryBookService;

    public List<CollectionBookDto> getCollectionBooks(Integer userId, Integer collectionId) {
        var collection = collectionService.getExistingById(collectionId);
        if (!collection.getUser().getId().equals(userId))
            throw new BadRequestException("Collection does not belong to user");

        return mapper.toDto(repository.findByIdCollectionId(collectionId));
    }

    public CollectionBookDto addBookToCollection(Integer userId, Integer collectionId, Integer libraryBookId) {
        var collection = collectionService.getExistingById(collectionId);
        if (!collection.getUser().getId().equals(userId))
            throw new BadRequestException("Collection does not belong to user");

        var libraryBook = libraryBookService.getExistingById(libraryBookId, userId);
        if (repository.existsById(new CollectionBookId(collection.getId(), libraryBook.getId())))
            throw new BadRequestException("Book is already added to collection");

        var collectionBook = repository.save(CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook.getId()))
                .libraryBook(libraryBook)
                .collection(collection)
                .build());
        return mapper.toDto(collectionBook);
    }

    public void removeBookFromCollection(Integer userId, Integer collectionId, Integer libraryBookId) {
        var collection = collectionService.getExistingById(collectionId);
        if (!collection.getUser().getId().equals(userId))
            throw new BadRequestException("Collection does not belong to user");

        var libraryBook = libraryBookService.getExistingById(libraryBookId, userId);
        repository.deleteById(new CollectionBookId(collection.getId(), libraryBook.getId()));
    }

    @Transactional
    public void removeBookFromAllCollections(Integer userId, Integer libraryBookId) {
        repository.deleteAllByIdLibraryBookIdAndLibraryBookUserId(libraryBookId, userId);
    }

    @Transactional
    public CollectionBookDto moveBook(Integer userId, Integer libraryBookId, Integer fromCollectionId, Integer toCollectionId) {
        removeBookFromCollection(userId, fromCollectionId, libraryBookId);
        return addBookToCollection(userId, toCollectionId, libraryBookId);
    }

}

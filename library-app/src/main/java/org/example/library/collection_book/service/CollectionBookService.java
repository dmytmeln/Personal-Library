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
        collectionService.verifyBelongsToUser(collection, userId);
        return mapper.toDto(repository.findByIdCollectionId(collectionId));
    }

    public CollectionBookDto addBookToCollection(Integer userId, Integer collectionId, Integer bookId) {
        var collection = collectionService.getExistingById(collectionId);
        collectionService.verifyBelongsToUser(collection, userId);
        var libraryBook = libraryBookService.getExistingById(bookId, userId);
        verifyNotExists(new CollectionBookId(collection.getId(), libraryBook.getId()));
        var collectionBook = repository.save(CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook.getId()))
                .libraryBook(libraryBook)
                .collection(collection)
                .build());
        return mapper.toDto(collectionBook);
    }

    public void removeBookFromCollection(Integer userId, Integer collectionId, Integer bookId) {
        var collection = collectionService.getExistingById(collectionId);
        collectionService.verifyBelongsToUser(collection, userId);
        var libraryBook = libraryBookService.getExistingById(bookId, userId);
        repository.deleteById(new CollectionBookId(collection.getId(), libraryBook.getId()));
    }

    private void verifyNotExists(CollectionBookId collectionBookId) {
        if (repository.existsById(collectionBookId)) {
            throw new BadRequestException("Book is already added to collection");
        }
    }

}

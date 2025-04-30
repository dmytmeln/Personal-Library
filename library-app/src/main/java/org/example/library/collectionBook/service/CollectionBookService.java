package org.example.library.collectionBook.service;

import lombok.RequiredArgsConstructor;
import org.example.library.book.service.BookService;
import org.example.library.collection.service.CollectionService;
import org.example.library.collectionBook.domain.CollectionBook;
import org.example.library.collectionBook.domain.CollectionBookId;
import org.example.library.collectionBook.dto.CollectionBookDto;
import org.example.library.collectionBook.mapper.CollectionBookMapper;
import org.example.library.collectionBook.repository.CollectionBookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionBookService {

    private final CollectionBookRepository repository;
    private final CollectionBookMapper mapper;
    private final CollectionService collectionService;
    private final BookService bookService;

    public List<CollectionBookDto> getCollectionBooks(Integer userId, Integer collectionId) {
        var collection = collectionService.getExistingById(collectionId);
        collectionService.verifyBelongsToUser(userId, collection);
        return mapper.toDto(repository.findByIdCollectionId(collectionId));
    }

    public CollectionBookDto addBookToCollection(Integer userId, Integer collectionId, Integer bookId) {
        var collection = collectionService.getExistingById(collectionId);
        collectionService.verifyBelongsToUser(userId, collection);
        var book = bookService.getExistingById(bookId);
        var collectionBook = repository.save(CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), book.getId()))
                .book(book)
                .collection(collection)
                .build());
        return mapper.toDto(collectionBook);
    }

    public void removeBookFromCollection(Integer userId, Integer collectionId, Integer bookId) {
        var collection = collectionService.getExistingById(collectionId);
        collectionService.verifyBelongsToUser(userId, collection);
        var book = bookService.getExistingById(bookId);
        repository.deleteById(new CollectionBookId(collection.getId(), book.getId()));
    }

}

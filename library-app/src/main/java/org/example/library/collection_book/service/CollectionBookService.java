package org.example.library.collection_book.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.library.collection.service.CollectionService;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.example.library.collection_book.dto.CollectionBookDto;
import org.example.library.collection_book.dto.CollectionBookSearchParams;
import org.example.library.collection_book.mapper.CollectionBookMapper;
import org.example.library.collection_book.repository.CollectionBookRepository;
import org.example.library.exception.BadRequestException;
import org.example.library.exception.NotFoundException;
import org.example.library.library_book.dto.LibraryBookDto;
import org.example.library.library_book.mapper.LibraryBookMapper;
import org.example.library.library_book.repository.LibraryBookRepository;
import org.example.library.library_book.repository.LibraryBookViewRepository;
import org.example.library.library_book.service.LibraryBookService;
import org.example.library.pagination.PageRequestBuilder;
import org.example.library.pagination.PaginationParams;
import org.example.library.pagination.SortableFields;
import org.springframework.data.domain.Page;
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
    private final LibraryBookRepository libraryBookRepository;
    private final EntityManager entityManager;
    private final LibraryBookViewRepository viewRepository;
    private final PageRequestBuilder pageRequestBuilder;
    private final LibraryBookMapper libraryBookMapper;


    @Transactional(readOnly = true)
    public Page<LibraryBookDto> getCollectionBooksPaginated(Integer userId, Integer collectionId, CollectionBookSearchParams searchParams, PaginationParams paginationParams) {
        var collection = collectionService.getExistingById(collectionId);
        if (!collection.getUser().getId().equals(userId))
            throw new BadRequestException("error.collection.not_belong_to_user");

        var pageable = pageRequestBuilder.buildPageRequest(paginationParams, SortableFields.LIBRARY_BOOK_FIELDS);

        return viewRepository.findCollectionBooks(userId, collectionId, searchParams, pageable)
                .map(libraryBookMapper::toDto);
    }

    @Transactional
    public CollectionBookDto addBookToCollection(Integer userId, Integer collectionId, Integer libraryBookId) {
        var collection = collectionService.getExistingById(collectionId);
        if (!collection.getUser().getId().equals(userId))
            throw new BadRequestException("error.collection.not_belong_to_user");

        var libraryBook = libraryBookService.getExistingById(libraryBookId, userId);
        var id = new CollectionBookId(collection.getId(), libraryBook.getId());
        if (repository.existsById(id))
            throw new BadRequestException("error.collection.book_already_added");

        var managedEntity = repository.saveAndFlush(CollectionBook.builder()
                .id(id)
                .libraryBook(libraryBook)
                .collection(collection)
                .build());
        entityManager.refresh(managedEntity);
        return mapper.toDto(managedEntity);
    }

    @Transactional
    public void bulkAddBooksToCollection(Integer userId, Integer collectionId, List<Integer> libraryBookIds) {
        var collection = collectionService.getExistingById(collectionId);
        if (!collection.getUser().getId().equals(userId))
            throw new BadRequestException("error.collection.not_belong_to_user");

        var libraryBooks = libraryBookRepository.findAllByIdInAndUserId(libraryBookIds, userId);
        if (libraryBooks.isEmpty())
            throw new NotFoundException("error.library_book.none_found");

        var existingInCollection = repository.findLibraryBookIdsByCollectionId(collectionId);

        var newMappings = libraryBooks.stream()
                .filter(lb -> !existingInCollection.contains(lb.getId()))
                .map(lb -> CollectionBook.builder()
                        .id(new CollectionBookId(collectionId, lb.getId()))
                        .libraryBook(lb)
                        .collection(collection)
                        .build())
                .toList();

        if (!newMappings.isEmpty()) {
            repository.saveAll(newMappings);
        }
    }

    public void removeBookFromCollection(Integer userId, Integer collectionId, Integer libraryBookId) {
        var collection = collectionService.getExistingById(collectionId);
        if (!collection.getUser().getId().equals(userId))
            throw new BadRequestException("error.collection.not_belong_to_user");

        var libraryBook = libraryBookService.getExistingById(libraryBookId, userId);
        repository.deleteById(new CollectionBookId(collection.getId(), libraryBook.getId()));
    }

    @Transactional
    public void bulkRemoveBooksFromCollection(Integer userId, Integer collectionId, List<Integer> libraryBookIds) {
        repository.deleteAllByCollectionIdAndLibraryBookIdInAndUserId(collectionId, libraryBookIds, userId);
    }

    @Transactional
    public void removeBookFromAllCollections(Integer userId, Integer libraryBookId) {
        repository.deleteByLibraryBookIdAndUserId(libraryBookId, userId);
    }

    @Transactional
    public CollectionBookDto moveBook(Integer userId, Integer libraryBookId, Integer fromCollectionId, Integer toCollectionId) {
        removeBookFromCollection(userId, fromCollectionId, libraryBookId);
        return addBookToCollection(userId, toCollectionId, libraryBookId);
    }

}

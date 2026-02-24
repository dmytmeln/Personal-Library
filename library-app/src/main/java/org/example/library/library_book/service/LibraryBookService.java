package org.example.library.library_book.service;

import lombok.RequiredArgsConstructor;
import org.example.library.book.service.BookService;
import org.example.library.collection_book.repository.CollectionBookRepository;
import org.example.library.exception.BadRequestException;
import org.example.library.exception.NotFoundException;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.domain.LibraryBookStatus;
import org.example.library.library_book.domain.LibraryBookView;
import org.example.library.library_book.dto.LibraryBookDto;
import org.example.library.library_book.dto.LibraryBookSearchCriteria;
import org.example.library.library_book.dto.UpdateLibraryBookDetailsDto;
import org.example.library.library_book.mapper.LibraryBookMapper;
import org.example.library.library_book.repository.LibraryBookRepository;
import org.example.library.library_book.repository.LibraryBookViewRepository;
import org.example.library.library_book.repository.LibraryBookViewSpecification;
import org.example.library.pagination.PageRequestBuilder;
import org.example.library.pagination.PaginationParams;
import org.example.library.pagination.SortableFields;
import org.example.library.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LibraryBookService {

    private static final int RATING_LOWER_BOUND = 0;
    private static final int RATING_UPPER_BOUND = 5;


    private final LibraryBookRepository repository;
    private final LibraryBookViewRepository viewRepository;
    private final LibraryBookMapper mapper;
    private final CollectionBookRepository collectionBookRepository;
    private final BookService bookService;
    private final PageRequestBuilder pageRequestBuilder;


    @Transactional(readOnly = true)
    public Page<LibraryBookDto> getAllByUserId(Integer userId, LibraryBookSearchCriteria criteria, PaginationParams paginationParams) {
        var spec = LibraryBookViewSpecification.fromSearchCriteria(userId, criteria);
        var pageable = pageRequestBuilder.buildPageRequest(paginationParams, SortableFields.LIBRARY_BOOK_FIELDS);

        return viewRepository.findAll(spec, pageable)
                .map(mapper::toDto);
    }

    public LibraryBook getExistingById(Integer libraryBookId, Integer userId) {
        return repository.findByIdAndUserId(libraryBookId, userId)
                .orElseThrow(() -> new NotFoundException("There is no such book in your library"));
    }

    @Transactional
    public LibraryBookDto create(Integer bookId, User user) {
        verifyNotExists(bookId, user.getId());
        var book = bookService.getExistingById(bookId);
        var saved = repository.saveAndFlush(LibraryBook.of(book, user));

        var view = getViewById(saved.getId());
        return mapper.toDto(view);
    }

    @Transactional
    public LibraryBookDto rate(Integer libraryBookId, Integer userId, Integer rating) {
        verifyRatingIsValid(rating);
        var libraryBook = getExistingById(libraryBookId, userId);
        libraryBook.setRating(rating.byteValue());
        repository.saveAndFlush(libraryBook);

        var view = getViewById(libraryBookId);
        return mapper.toDto(view);
    }

    @Transactional
    public LibraryBookDto updateStatus(Integer libraryBookId, Integer userId, LibraryBookStatus status) {
        var libraryBook = getExistingById(libraryBookId, userId);
        libraryBook.setStatus(status);
        repository.saveAndFlush(libraryBook);

        var view = getViewById(libraryBookId);
        return mapper.toDto(view);
    }

    @Transactional
    public LibraryBookDto updateDetails(Integer libraryBookId, Integer userId, UpdateLibraryBookDetailsDto dto) {
        var libraryBook = getExistingById(libraryBookId, userId);
        mapper.update(libraryBook, dto);
        repository.saveAndFlush(libraryBook);
        var updatedView = getViewById(libraryBookId);
        return mapper.toDto(updatedView);
    }

    @Transactional
    public LibraryBookDto resetDetails(Integer libraryBookId, Integer userId) {
        var libraryBook = getExistingById(libraryBookId, userId);
        libraryBook.resetOverriddenFields();
        repository.saveAndFlush(libraryBook);
        var updatedView = getViewById(libraryBookId);
        return mapper.toDto(updatedView);
    }

    @Transactional
    public void delete(Integer libraryBookId, Integer userId) {
        collectionBookRepository.deleteByLibraryBookIdAndUserId(libraryBookId, userId);
        repository.deleteByIdAndUserId(libraryBookId, userId);
    }

    public Map.Entry<Double, Integer> getBookAverageRatingAndCount(Integer bookId) {
        var bookRatings = repository.findBookRatings(bookId).stream()
                .filter(Objects::nonNull)
                .toList();
        var averageRating = bookRatings.stream()
                .mapToInt(r -> r)
                .average()
                .orElse(0);
        return new AbstractMap.SimpleEntry<>(averageRating, bookRatings.size());
    }

    public Optional<LibraryBookStatus> getBookStatus(Integer bookId, Integer userId) {
        return repository.findStatusByBookIdAndUserId(bookId, userId); // todo refactor
    }

    public Optional<Integer> getUserRatingOfBook(Integer bookId, Integer userId) {
        return repository.findUserRatingOfBook(bookId, userId); // todo refactor
    }

    private LibraryBookView getViewById(Integer libraryBookId) {
        return viewRepository.findById(libraryBookId)
                .orElseThrow(() -> new NotFoundException("Library book view not found"));
    }

    private void verifyRatingIsValid(Integer rating) {
        if (rating < RATING_LOWER_BOUND || rating > RATING_UPPER_BOUND) {
            throw new BadRequestException("Rating should be between %d and %d"
                    .formatted(RATING_LOWER_BOUND, RATING_UPPER_BOUND));
        }
    }

    private void verifyNotExists(Integer bookId, Integer userId) {
        if (repository.existsByBookIdAndUserId(bookId, userId)) {
            throw new BadRequestException("Book already added to your library");
        }
    }

}

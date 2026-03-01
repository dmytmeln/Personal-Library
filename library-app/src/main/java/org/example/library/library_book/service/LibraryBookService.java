package org.example.library.library_book.service;

import lombok.RequiredArgsConstructor;
import org.example.library.book.dto.LanguageWithCount;
import org.example.library.book.repository.BookRepository;
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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LibraryBookService {

    private static final int RATING_LOWER_BOUND = 0;
    private static final int RATING_UPPER_BOUND = 5;


    private final LibraryBookRepository repository;
    private final LibraryBookViewRepository viewRepository;
    private final CollectionBookRepository collectionBookRepository;
    private final BookRepository bookRepository;
    private final LibraryBookMapper mapper;
    private final PageRequestBuilder pageRequestBuilder;


    @Transactional(readOnly = true)
    public Page<LibraryBookDto> getAllByUserId(Integer userId, LibraryBookSearchCriteria criteria, PaginationParams paginationParams) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        var spec = LibraryBookViewSpecification.fromSearchCriteria(userId, lang, criteria);
        var pageable = pageRequestBuilder.buildPageRequest(paginationParams, SortableFields.LIBRARY_BOOK_FIELDS);

        return viewRepository.findAll(spec, pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<LanguageWithCount> getLanguagesByUserId(Integer userId) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        return repository.findLanguagesWithCountByUserId(userId, lang);
    }

    @Transactional
    public void create(Integer bookId, User user) {
        if (repository.existsByBookIdAndUserId(bookId, user.getId()))
            throw new BadRequestException("error.library_book.already_added");

        if (!bookRepository.existsById(bookId))
            throw new NotFoundException("error.book.not_found");

        repository.save(LibraryBook.of(bookRepository.getReferenceById(bookId), user));
    }

    @Transactional
    public void bulkAdd(List<Integer> bookIds, User user) {
        var existingIds = repository.findExistingBookIdsInLibrary(user.getId(), bookIds);
        var newBookIds = bookIds.stream()
                .filter(id -> !existingIds.contains(id))
                .distinct()
                .toList();

        if (newBookIds.isEmpty()) return;

        var books = bookRepository.findAllById(bookIds);
        if (books.isEmpty())
            throw new NotFoundException("error.book.none_found");

        var libraryBooks = books.stream()
                .map(book -> LibraryBook.of(book, user))
                .toList();

        if (!libraryBooks.isEmpty()) {
            repository.saveAll(libraryBooks);
        }
    }

    @Transactional
    public LibraryBookDto rate(Integer libraryBookId, Integer userId, Integer rating) {
        if (rating < RATING_LOWER_BOUND || rating > RATING_UPPER_BOUND)
            throw new BadRequestException("error.library_book.invalid_rating");

        int updatedCount = repository.updateRating(libraryBookId, userId, rating.byteValue());
        if (updatedCount == 0)
            throw new NotFoundException("error.library_book.not_found");

        repository.flush();
        var view = getViewById(libraryBookId);
        return mapper.toDto(view);
    }

    @Transactional
    public LibraryBookDto updateStatus(Integer libraryBookId, Integer userId, LibraryBookStatus status) {
        var libraryBook = getExistingById(libraryBookId, userId);
        var oldStatus = libraryBook.getStatus();

        if (status == LibraryBookStatus.READ && oldStatus != LibraryBookStatus.READ) {
            libraryBook.setFinishedAt(LocalDate.now());
        } else if (status != LibraryBookStatus.READ && oldStatus == LibraryBookStatus.READ) {
            libraryBook.setFinishedAt(null);
        }

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
        int updatedCount = repository.resetOverriddenFields(libraryBookId, userId);
        if (updatedCount == 0)
            throw new NotFoundException("error.library_book.not_found");

        repository.flush();
        var updatedView = getViewById(libraryBookId);
        return mapper.toDto(updatedView);
    }

    @Transactional
    public void delete(Integer libraryBookId, Integer userId) {
        collectionBookRepository.deleteByLibraryBookIdAndUserId(libraryBookId, userId);
        repository.deleteByIdAndUserId(libraryBookId, userId);
    }

    @Transactional
    public void bulkDelete(List<Integer> libraryBookIds, Integer userId) {
        collectionBookRepository.deleteAllByLibraryBookIdInAndUserId(libraryBookIds, userId);
        repository.deleteAllByIdInAndUserId(libraryBookIds, userId);
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

    private LibraryBook getExistingById(Integer libraryBookId, Integer userId) {
        return repository.findByIdAndUserId(libraryBookId, userId)
                .orElseThrow(() -> new NotFoundException("error.library_book.not_found"));
    }

    private LibraryBookView getViewById(Integer libraryBookId) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        return viewRepository.findByIdAndLanguageCode(libraryBookId, lang)
                .orElseThrow(() -> new NotFoundException("error.library_book.view_not_found"));
    }

}

package org.example.library.library_book.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.book.dto.LanguageWithCount;
import org.example.library.book.repository.BookRepository;
import org.example.library.category.repository.CategoryRepository;
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
import org.example.library.recommendations.event.UserProfileUpdatedEvent;
import org.example.library.user.domain.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryBookService {

    private static final int RATING_LOWER_BOUND = 0;
    private static final int RATING_UPPER_BOUND = 5;


    private final LibraryBookRepository repository;
    private final LibraryBookViewRepository viewRepository;
    private final CollectionBookRepository collectionBookRepository;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final LibraryBookMapper mapper;
    private final PageRequestBuilder pageRequestBuilder;
    private final ApplicationEventPublisher eventPublisher;


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
        incrementPopularity(List.of(bookId));
        eventPublisher.publishEvent(new UserProfileUpdatedEvent(user.getId()));
        log.info("[LIBRARY_BOOK_ADD] User ID: {}, Book ID: {}", user.getId(), bookId);
    }

    @Transactional
    public void bulkAdd(List<Integer> bookIds, User user) {
        var existingIds = repository.findExistingBookIdsInLibrary(user.getId(), bookIds);
        var newBookIds = bookIds.stream()
                .filter(id -> !existingIds.contains(id))
                .distinct()
                .toList();

        if (newBookIds.isEmpty()) return;

        var books = bookRepository.findAllById(newBookIds);
        if (books.isEmpty())
            throw new NotFoundException("error.book.none_found");

        var libraryBooks = books.stream()
                .map(book -> LibraryBook.of(book, user))
                .toList();

        repository.saveAll(libraryBooks);
        incrementPopularity(newBookIds);
        eventPublisher.publishEvent(new UserProfileUpdatedEvent(user.getId()));
        log.info("[LIBRARY_BOOK_BULK_ADD] User ID: {}, Book IDs: {}", user.getId(), newBookIds);
    }

    @Transactional
    public LibraryBookDto rate(Integer libraryBookId, Integer userId, Integer rating) {
        if (rating < RATING_LOWER_BOUND || rating > RATING_UPPER_BOUND)
            throw new BadRequestException("error.library_book.invalid_rating");

        int updatedCount = repository.updateRating(libraryBookId, userId, rating.byteValue());
        if (updatedCount == 0)
            throw new NotFoundException("error.library_book.not_found");

        repository.flush();
        eventPublisher.publishEvent(new UserProfileUpdatedEvent(userId));
        var view = getViewById(libraryBookId);
        log.info("[LIBRARY_BOOK_RATE] User ID: {}, Library Book ID: {}, Rating: {}", userId, libraryBookId, rating);
        return mapper.toDto(view);
    }

    @Transactional
    public LibraryBookDto updateStatus(Integer libraryBookId, Integer userId, LibraryBookStatus status) {
        var libraryBook = getExistingById(libraryBookId, userId);
        updateBookStatus(libraryBook, status);
        repository.saveAndFlush(libraryBook);

        eventPublisher.publishEvent(new UserProfileUpdatedEvent(userId));
        var view = getViewById(libraryBookId);
        log.info("[LIBRARY_BOOK_STATUS_UPDATE] User ID: {}, Library Book ID: {}, Status: {}", userId, libraryBookId, status);
        return mapper.toDto(view);
    }

    @Transactional
    public void bulkUpdateStatus(List<Integer> libraryBookIds, Integer userId, LibraryBookStatus status) {
        var libraryBooks = repository.findAllByIdInAndUserId(libraryBookIds, userId);
        if (libraryBooks.isEmpty()) return;

        libraryBooks.forEach(lb -> updateBookStatus(lb, status));
        repository.saveAll(libraryBooks);
        eventPublisher.publishEvent(new UserProfileUpdatedEvent(userId));
        log.info("[LIBRARY_BOOK_BULK_STATUS_UPDATE] User ID: {}, Library Book IDs: {}, Status: {}", userId, libraryBookIds, status);
    }

    private void updateBookStatus(LibraryBook libraryBook, LibraryBookStatus status) {
        var oldStatus = libraryBook.getStatus();

        if (status == LibraryBookStatus.READ && oldStatus != LibraryBookStatus.READ) {
            libraryBook.setFinishedAt(LocalDate.now());
        } else if (status != LibraryBookStatus.READ && oldStatus == LibraryBookStatus.READ) {
            libraryBook.setFinishedAt(null);
        }

        libraryBook.setStatus(status);
    }

    @Transactional
    public LibraryBookDto updateDetails(Integer libraryBookId, Integer userId, UpdateLibraryBookDetailsDto dto) {
        var libraryBook = getExistingById(libraryBookId, userId);
        mapper.update(libraryBook, dto);
        repository.saveAndFlush(libraryBook);
        var updatedView = getViewById(libraryBookId);
        log.info("[LIBRARY_BOOK_DETAILS_UPDATE] User ID: {}, Library Book ID: {}", userId, libraryBookId);
        return mapper.toDto(updatedView);
    }

    @Transactional
    public LibraryBookDto resetDetails(Integer libraryBookId, Integer userId) {
        int updatedCount = repository.resetOverriddenFields(libraryBookId, userId);
        if (updatedCount == 0)
            throw new NotFoundException("error.library_book.not_found");

        repository.flush();
        var updatedView = getViewById(libraryBookId);
        log.info("[LIBRARY_BOOK_DETAILS_RESET] User ID: {}, Library Book ID: {}", userId, libraryBookId);
        return mapper.toDto(updatedView);
    }

    @Transactional
    public void delete(Integer libraryBookId, Integer userId) {
        var libraryBook = repository.findByIdAndUserIdWithBook(libraryBookId, userId)
                .orElseThrow(() -> new NotFoundException("error.library_book.not_found"));
        var bookId = libraryBook.getBook().getId();
        collectionBookRepository.deleteByLibraryBookIdAndUserId(libraryBookId, userId);
        repository.delete(libraryBook);
        decrementPopularity(List.of(bookId));
        eventPublisher.publishEvent(new UserProfileUpdatedEvent(userId));
        log.info("[LIBRARY_BOOK_DELETE] User ID: {}, Library Book ID: {}", userId, libraryBookId);
    }

    @Transactional
    public void bulkDelete(List<Integer> libraryBookIds, Integer userId) {
        var libraryBooks = repository.findAllByIdInAndUserIdWithBook(libraryBookIds, userId);
        if (libraryBooks.isEmpty()) return;

        var bookIds = libraryBooks.stream().map(lb -> lb.getBook().getId()).toList();
        collectionBookRepository.deleteAllByLibraryBookIdInAndUserId(libraryBookIds, userId);
        repository.deleteAll(libraryBooks);
        decrementPopularity(bookIds);
        eventPublisher.publishEvent(new UserProfileUpdatedEvent(userId));
        log.info("[LIBRARY_BOOK_BULK_DELETE] User ID: {}, Library Book IDs: {}", userId, libraryBookIds);
    }

    private void incrementPopularity(List<Integer> bookIds) {
        bookRepository.incrementPopularityCount(bookIds);
        categoryRepository.incrementPopularityCountByBookIds(bookIds);
        authorRepository.incrementPopularityCountByBookIds(bookIds);
    }

    private void decrementPopularity(List<Integer> bookIds) {
        bookRepository.decrementPopularityCount(bookIds);
        categoryRepository.decrementPopularityCountByBookIds(bookIds);
        authorRepository.decrementPopularityCountByBookIds(bookIds);
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

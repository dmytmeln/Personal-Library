package org.example.library.library_book.service;

import lombok.RequiredArgsConstructor;
import org.example.library.book.service.BookService;
import org.example.library.collection_book.repository.CollectionBookRepository;
import org.example.library.exception.BadRequestException;
import org.example.library.exception.NotFoundException;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.domain.LibraryBookStatus;
import org.example.library.library_book.dto.LibraryBookDto;
import org.example.library.library_book.mapper.LibraryBookMapper;
import org.example.library.library_book.repository.LibraryBookRepository;
import org.example.library.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final LibraryBookMapper mapper;
    private final BookService bookService;
    private final CollectionBookRepository collectionBookRepository;


    public Page<LibraryBookDto> getAllByUserId(Integer userId, Pageable pageable) {
        return repository.findAllByUserId(userId, pageable)
                .map(mapper::toDto);
    }

    public LibraryBook getExistingById(Integer libraryBookId, Integer userId) {
        return repository.findByIdAndUserId(libraryBookId, userId)
                .orElseThrow(() -> new NotFoundException("There is no such book in your library"));
    }

    public LibraryBookDto create(Integer bookId, User user) {
        verifyNotExists(bookId, user.getId());
        var book = bookService.getExistingById(bookId);
        var saved = repository.save(LibraryBook.of(book, user));
        return mapper.toDto(saved);
    }

    public LibraryBookDto rate(Integer libraryBookId, Integer userId, Integer rating) {
        verifyRatingIsValid(rating);
        var libraryBook = getExistingById(libraryBookId, userId);
        libraryBook.setRating(rating.byteValue());
        return mapper.toDto(repository.save(libraryBook));
    }

    public LibraryBookDto updateStatus(Integer libraryBookId, Integer userId, LibraryBookStatus status) {
        var libraryBook = getExistingById(libraryBookId, userId);
        libraryBook.setStatus(status);
        return mapper.toDto(repository.save(libraryBook));
    }

    @Transactional
    public void delete(Integer libraryBookId, Integer userId) {
        var libraryBook = getExistingById(libraryBookId, userId);
        collectionBookRepository.deleteByLibraryBook(libraryBook);
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

package org.example.library.userBooks.service;

import lombok.RequiredArgsConstructor;
import org.example.library.book.service.BookService;
import org.example.library.exception.NotFoundException;
import org.example.library.userBooks.domain.UserBook;
import org.example.library.userBooks.domain.UserBookId;
import org.example.library.userBooks.domain.UserBookStatus;
import org.example.library.userBooks.dto.UserBookDto;
import org.example.library.userBooks.mapper.UserBookMapper;
import org.example.library.userBooks.repository.UserBookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBookService {

    private final UserBookRepository repository;
    private final UserBookMapper mapper;
    private final BookService bookService;

    public List<UserBookDto> getAllByUserId(Integer userId) {
        return mapper.toDto(repository.findAllByIdUserId(userId));
    }

    public UserBookDto getById(Integer userId, Integer bookId) {
        return mapper.toDto(getExistingById(new UserBookId(userId, bookId)));
    }

    public UserBookDto create(Integer userId, Integer bookId) {
        var book = bookService.getExistingById(bookId);
        var saved = repository.save(UserBook.builder()
                .id(new UserBookId(userId, bookId))
                .status(UserBookStatus.NO_TAG)
                .book(book)
                .build());
        return mapper.toDto(saved);
    }

    public UserBookDto rateBook(Integer userId, Integer bookId, Integer rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating should be between 0 and 5");
        }
        var userBook = getExistingById(new UserBookId(userId, bookId));
        userBook.setRating(rating.byteValue());
        return mapper.toDto(repository.save(userBook));
    }

    public UserBookDto updateStatus(Integer userId, Integer bookId, UserBookStatus status) {
        var userBook = getExistingById(new UserBookId(userId, bookId));
        userBook.setStatus(status);
        return mapper.toDto(repository.save(userBook));
    }

    public void delete(Integer userId, Integer bookId) {
        repository.deleteById(new UserBookId(userId, bookId));
    }

    private UserBook getExistingById(UserBookId userBookId) {
        return repository.findById(userBookId)
                .orElseThrow(() -> new NotFoundException("There is no such book in your library"));
    }

}

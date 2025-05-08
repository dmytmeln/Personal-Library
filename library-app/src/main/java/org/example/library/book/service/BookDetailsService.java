package org.example.library.book.service;

import lombok.RequiredArgsConstructor;
import org.example.library.book.dto.BookDetails;
import org.example.library.collection.service.CollectionService;
import org.example.library.library_book.service.LibraryBookService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookDetailsService {

    private final CollectionService collectionService;
    private final BookService bookService;
    private final LibraryBookService libraryBookService;

    public BookDetails getDetails(Integer bookId, Integer userId) {
        bookService.verifyExistsById(bookId);
        var collections = collectionService.getAllByUserIdAndBookId(userId, bookId);
        var bookAverageRatingAndCount = libraryBookService.getBookAverageRatingAndCount(bookId);
        var bookStatusOpt = libraryBookService.getBookStatus(bookId, userId);
        var userRating = libraryBookService.getUserRatingOfBook(bookId, userId)
                .orElse(0);
        var builder = BookDetails.builder();
        if (bookStatusOpt.isPresent()) {
            builder.status(bookStatusOpt.get());
            builder.isInLibrary(true);
        } else {
            builder.isInLibrary(false);
        }
        return builder
                .collections(collections)
                .averageRating(bookAverageRatingAndCount.getKey())
                .ratingsNumber(bookAverageRatingAndCount.getValue())
                .myRating(userRating)
                .build();
    }

}

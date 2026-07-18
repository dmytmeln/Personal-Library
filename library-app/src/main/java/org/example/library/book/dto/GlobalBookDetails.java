package org.example.library.book.dto;

import lombok.Getter;
import org.example.library.library_book.dto.BookRatingSummary;

@Getter
public class GlobalBookDetails extends BookDetails {

    private final BookDto book;

    private GlobalBookDetails(BookDto book, double averageRating, long ratingsNumber) {
        super(averageRating, ratingsNumber);
        this.book = book;
    }

    public static GlobalBookDetails from(BookDto book, BookRatingSummary ratingSummary) {
        return new GlobalBookDetails(book, ratingSummary.getAverageRating(), ratingSummary.getRatingsCount());
    }

}

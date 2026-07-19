package org.example.library.book.dto;

import lombok.Getter;
import org.example.library.collection.dto.BasicCollectionDto;
import org.example.library.library_book.dto.BookRatingSummary;
import org.example.library.library_book.dto.LibraryBookDto;

import java.util.List;

@Getter
public class LibraryBookDetails extends BookDetails {

    private final LibraryBookDto libraryBook;
    private final List<BasicCollectionDto> collections;

    private LibraryBookDetails(LibraryBookDto libraryBook, double averageRating, long ratingsNumber, List<BasicCollectionDto> collections) {
        super(averageRating, ratingsNumber);
        this.libraryBook = libraryBook;
        this.collections = collections;
    }

    public static LibraryBookDetails from(LibraryBookDto libraryBook,
                                          BookRatingSummary ratingSummary,
                                          List<BasicCollectionDto> collections) {
        return new LibraryBookDetails(libraryBook, ratingSummary.getAverageRating(), ratingSummary.getRatingsCount(), collections);
    }

}

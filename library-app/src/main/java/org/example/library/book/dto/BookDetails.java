package org.example.library.book.dto;

import lombok.Builder;
import org.example.library.collection.dto.BasicCollectionDto;
import org.example.library.library_book.dto.LibraryBookDto;

import java.util.List;

@Builder
public record BookDetails(
        BookDto book,
        LibraryBookDto libraryBook,
        double averageRating,
        long ratingsNumber,
        List<BasicCollectionDto> collections
) {
}

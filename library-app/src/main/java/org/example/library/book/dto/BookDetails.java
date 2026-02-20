package org.example.library.book.dto;

import lombok.Builder;
import org.example.library.collection.dto.BasicCollectionDto;
import org.example.library.library_book.domain.LibraryBookStatus;

import java.util.List;

@Builder
public record BookDetails(
        double averageRating,
        double ratingsNumber,
        double myRating,
        List<BasicCollectionDto> collections,
        LibraryBookStatus status,
        boolean isInLibrary
) {
}

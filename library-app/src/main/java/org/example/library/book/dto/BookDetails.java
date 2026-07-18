package org.example.library.book.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GlobalBookDetails.class, name = "GLOBAL"),
        @JsonSubTypes.Type(value = LibraryBookDetails.class, name = "LIBRARY")
})
@Getter
@RequiredArgsConstructor
public abstract class BookDetails {

    private final double averageRating;
    private final long ratingsNumber;

}

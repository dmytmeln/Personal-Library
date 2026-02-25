package org.example.library.pagination;

import org.example.library.author.domain.Author_;
import org.example.library.book.domain.Book_;
import org.example.library.category.domain.Category_;
import org.example.library.library_book.domain.LibraryBookView_;

import java.util.Set;

public class SortableFields {

    private static final String BOOKS_COUNT_FIELD = "booksCount";

    private SortableFields() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static final Set<String> BOOK_FIELDS = Set.of(
            Book_.ID, Book_.TITLE, Book_.PUBLISH_YEAR, Book_.LANGUAGE, Book_.PAGES,
            Book_.CATEGORY + '.' + Category_.ID, Book_.CATEGORY + '.' + Category_.NAME
    );

    public static final Set<String> AUTHOR_FIELDS = Set.of(
            Author_.ID, Author_.FULL_NAME, Author_.COUNTRY, Author_.BIRTH_YEAR, Author_.DEATH_YEAR, BOOKS_COUNT_FIELD
    );

    public static final Set<String> CATEGORY_FIELDS = Set.of(
            Category_.ID, Category_.NAME, BOOKS_COUNT_FIELD
    );

    public static final Set<String> LIBRARY_BOOK_FIELDS = Set.of(
            LibraryBookView_.ID, LibraryBookView_.STATUS, LibraryBookView_.ADDED_AT,
            LibraryBookView_.RATING, LibraryBookView_.TITLE, LibraryBookView_.PUBLISH_YEAR,
            LibraryBookView_.PAGES, LibraryBookView_.LANGUAGE, LibraryBookView_.CATEGORY_NAME
    );

}

package org.example.library.pagination;

import java.util.Set;

public class SortableFields {

    private SortableFields() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static final Set<String> BOOK_FIELDS = Set.of(
            "id", "title", "publishYear", "language", "pages", "category.id", "category.name"
    );

    public static final Set<String> AUTHOR_FIELDS = Set.of(
            "id", "fullName", "country", "birthYear", "deathYear", "booksCount"
    );

    public static final Set<String> CATEGORY_FIELDS = Set.of(
            "id", "name", "booksCount"
    );

}

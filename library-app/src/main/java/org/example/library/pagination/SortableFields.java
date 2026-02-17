package org.example.library.pagination;

import java.util.Set;

public class SortableFields {

    private SortableFields() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static final Set<String> BOOK_FIELDS = Set.of(
            "id", "title", "publishYear", "language", "pages", "category.id", "category.name"
    );

}

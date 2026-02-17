package org.example.library.pagination;

import org.example.library.exception.InvalidSortParameterException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class SortValidator {

    private static final Set<String> ALLOWED_DIRECTIONS = Set.of("ASC", "DESC");


    public void validateSortParameters(List<String> sort, Set<String> allowedFields) {
        if (sort == null || sort.isEmpty())
            return;

        for (String sortParam : sort) {
            var parts = sortParam.split(",");
            var field = parts[0].trim();

            if (parts.length > 2) {
                throw new InvalidSortParameterException(
                        "Invalid sort parameter format: " + sortParam +
                                ". Expected format: field,direction"
                );
            }

            if (!allowedFields.contains(field)) {
                throw new InvalidSortParameterException(
                        "Invalid sort field: " + field +
                                ". Allowed fields: " + allowedFields
                );
            }

            if (parts.length > 1) {
                var direction = parts[1].trim().toUpperCase();
                if (!ALLOWED_DIRECTIONS.contains(direction)) {
                    throw new InvalidSortParameterException(
                            "Invalid sort direction: " + direction +
                                    ". Use some of " + ALLOWED_DIRECTIONS
                    );
                }
            }
        }
    }

}

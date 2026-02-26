
package org.example.library.collection.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class AddBookToCollectionsRequest {
    @NotNull(message = "{validation.library_book.id.required}")
    @Positive(message = "{validation.library_book.id.positive}")
    private Integer libraryBookId;
    @NotEmpty(message = "{validation.collection.ids.not_empty}")
    private List<Integer> collectionIds;
}

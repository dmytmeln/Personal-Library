
package org.example.library.collection.dto;

import lombok.Data;
import java.util.List;

@Data
public class AddBookToCollectionsRequest {
    private Integer libraryBookId;
    private List<Integer> collectionIds;
}


package org.example.library.collection.dto;

import lombok.Data;
import java.util.List;

@Data
public class BulkMoveRequest {
    private List<Integer> collectionIdsToMove;
    private Integer newParentId;
}

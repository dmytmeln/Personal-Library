
package org.example.library.collection.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class BulkMoveRequest {
    @NotEmpty(message = "{validation.collection.ids.not_empty}")
    private List<Integer> collectionIdsToMove;
    @NotNull(message = "{validation.collection.id.required}")
    @Positive(message = "{validation.collection.id.positive}")
    private Integer newParentId;
}

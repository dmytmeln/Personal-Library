

package org.example.library.collection.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MoveCollectionRequest {
    @NotNull(message = "{validation.collection.id.required}")
    @Positive(message = "{validation.collection.id.positive}")
    private Integer newParentId;
}

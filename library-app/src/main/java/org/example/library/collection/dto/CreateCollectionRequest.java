
package org.example.library.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCollectionRequest {
    @NotBlank(message = "{validation.collection.name.required}")
    @Size(min = 1, max = 100, message = "{validation.collection.name.size}")
    private String name;
    @Size(max = 500, message = "{validation.collection.description.size}")
    private String description;
    @Positive(message = "{validation.collection.id.positive}")
    private Integer parentId;
}

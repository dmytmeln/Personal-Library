
package org.example.library.collection.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.library.validation.AtLeastOneNotNull;

@Data
@AtLeastOneNotNull(fieldNames = {"name", "description", "color"})
public class UpdateCollectionDto {
    @Size(min = 1, max = 100, message = "{validation.collection.name.size}")
    private String name;
    @Size(max = 500, message = "{validation.collection.description.size}")
    private String description;
    @Size(max = 7, message = "{validation.collection.color.size}")
    private String color;
}


package org.example.library.collection.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BasicCollectionDto {
    private Integer id;
    private String name;
    private Integer parentId;
    private String color;
    private LocalDateTime updatedAt;
}

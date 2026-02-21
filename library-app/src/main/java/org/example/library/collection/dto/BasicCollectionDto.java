
package org.example.library.collection.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BasicCollectionDto {
    private Integer id;
    private String name;
    private Integer parentId;
}

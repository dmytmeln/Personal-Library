
package org.example.library.collection.dto;

import lombok.Builder;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CollectionNodeDto {
    private Integer id;
    private String name;
    
    @Builder.Default
    private List<CollectionNodeDto> children = new ArrayList<>();
}

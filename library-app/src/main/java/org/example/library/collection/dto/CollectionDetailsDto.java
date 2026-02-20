
package org.example.library.collection.dto;

import lombok.Builder;
import lombok.Data;
import org.example.library.collection_book.dto.CollectionBookDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CollectionDetailsDto {
    private Integer id;
    private String name;
    private String description;
    private String color;
    private LocalDateTime createdAt;
    private List<BasicCollectionDto> children;
    private List<CollectionBookDto> books;
}

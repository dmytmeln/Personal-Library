package org.example.library.collection.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CollectionDetailsDto {
    private Integer id;
    private String name;
    private String description;
    private Integer parentId;
    private LocalDateTime createdAt;
    private List<BasicCollectionDto> ancestors;
    private List<BasicCollectionDto> children;
}

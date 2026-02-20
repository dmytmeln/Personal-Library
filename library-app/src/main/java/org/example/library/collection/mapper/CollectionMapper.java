
package org.example.library.collection.mapper;

import org.example.library.collection.domain.Collection;
import org.example.library.collection.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CollectionMapper {

    BasicCollectionDto toBasicDto(Collection collection);

    List<BasicCollectionDto> toBasicDto(List<Collection> collections);

    @Mapping(target = "children", ignore = true)
    CollectionNodeDto toNodeDto(Collection collection);

    List<CollectionNodeDto> toNodeDto(List<Collection> collections);

    @Mapping(target = "books", ignore = true)
    CollectionDetailsDto toDetailsDto(Collection collection);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    Collection toEntity(CreateCollectionRequest createCollectionRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    void updateFromDto(UpdateCollectionDto updateCollectionDto, @MappingTarget Collection collection);

}

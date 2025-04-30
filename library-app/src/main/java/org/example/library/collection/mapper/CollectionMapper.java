package org.example.library.collection.mapper;

import org.example.library.collection.domain.Collection;
import org.example.library.collection.dto.CollectionDto;
import org.example.library.collection.dto.CreateCollectionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CollectionMapper {

    CollectionDto toDto(Collection collection);

    List<CollectionDto> toDto(List<Collection> collections);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Collection toEntity(CreateCollectionRequest createCollectionRequest);

}

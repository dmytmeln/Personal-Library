package org.example.library.collection.service;

import lombok.RequiredArgsConstructor;
import org.example.library.collection.domain.Collection;
import org.example.library.collection.dto.CollectionDto;
import org.example.library.collection.dto.CreateCollectionRequest;
import org.example.library.collection.mapper.CollectionMapper;
import org.example.library.collection.repository.CollectionRepository;
import org.example.library.exception.BadRequestException;
import org.example.library.exception.NotFoundException;
import org.example.library.user.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository repository;
    private final CollectionMapper mapper;

    public List<CollectionDto> getAllByUserId(Integer userId) {
        return mapper.toDto(repository.findAllByUserId(userId));
    }

    public List<CollectionDto> getAllByUserIdAndBookId(Integer userId, Integer bookId) {
        return mapper.toDto(repository.findAllByUserIdAndBookId(userId, bookId));
    }

    public Collection getExistingById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Collection not found"));
    }

    public CollectionDto getById(Integer userId, Integer collectionId) {
        var collection = getExistingById(collectionId);
        verifyBelongsToUser(collection, userId);
        return mapper.toDto(collection);
    }

    public CollectionDto createCollection(CreateCollectionRequest dto, User user) {
        var collection = mapper.toEntity(dto);
        collection.setUser(user);
        return mapper.toDto(repository.save(collection));
    }

    public CollectionDto updateCollection(Integer collectionId, CreateCollectionRequest dto, Integer userId) {
        var collection = getExistingById(collectionId);
        verifyBelongsToUser(collection, userId);
        var updatedCollection = repository.save(
                collection.toBuilder()
                        .name(dto.name())
                        .description(dto.description())
                        .color(dto.color())
                        .build());
        return mapper.toDto(updatedCollection);
    }

    public void verifyBelongsToUser(Collection collection, Integer userId) {
        if (!collection.getUser().getId().equals(userId)) {
            throw new BadRequestException("Collection does not belong to user");
        }
    }

}

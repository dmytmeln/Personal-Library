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

    public Collection getExistingById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Collection not found"));
    }

    public CollectionDto createCollection(CreateCollectionRequest dto, User user) {
        var collection = mapper.toEntity(dto);
        collection.setUser(user);
        return mapper.toDto(repository.save(collection));
    }

    public CollectionDto getById(Integer userId, Integer collectionId) {
        var collection = getExistingById(collectionId);
        verifyBelongsToUser(userId, collection);
        return mapper.toDto(collection);
    }

    public void verifyBelongsToUser(Integer userId, Collection collection) {
        if (!collection.getUser().getId().equals(userId)) {
            throw new BadRequestException("Collection does not belong to user");
        }
    }

}

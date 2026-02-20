
package org.example.library.collection.service;

import lombok.RequiredArgsConstructor;
import org.example.library.collection.domain.Collection;
import org.example.library.collection.dto.*;
import org.example.library.collection.mapper.CollectionMapper;
import org.example.library.collection.repository.CollectionRepository;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.example.library.collection_book.mapper.CollectionBookMapper;
import org.example.library.collection_book.repository.CollectionBookRepository;
import org.example.library.exception.BadRequestException;
import org.example.library.exception.NotFoundException;
import org.example.library.library_book.repository.LibraryBookRepository;
import org.example.library.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionBookRepository collectionBookRepository;
    private final LibraryBookRepository libraryBookRepository;
    private final UserRepository userRepository;
    private final CollectionMapper collectionMapper;
    private final CollectionBookMapper collectionBookMapper;


    @Transactional(readOnly = true)
    public List<BasicCollectionDto> getAllByUserIdAndBookId(Integer userId, Integer bookId) {
        return collectionMapper.toBasicDto(collectionRepository.findAllByUserIdAndBookId(userId, bookId));
    }

    @Transactional(readOnly = true)
    public Collection getExistingById(Integer id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Collection not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<CollectionNodeDto> getUserCollectionTree(Integer userId) {
        var allCollections = collectionRepository.findAllByUserId(userId);
        var collectionsLookupMap = allCollections.stream()
                .collect(Collectors.toMap(Collection::getId, collectionMapper::toNodeDto));

        var rootNodes = new ArrayList<CollectionNodeDto>();
        for (var collection : allCollections) {
            var currentDto = collectionsLookupMap.get(collection.getId());
            if (collection.getParent() == null) {
                rootNodes.add(currentDto);
            } else {
                var parentDto = collectionsLookupMap.get(collection.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(currentDto);
                }
            }
        }
        return rootNodes;
    }

    @Transactional(readOnly = true)
    public CollectionDetailsDto getCollectionDetails(Integer collectionId, Integer userId) {
        var collection = collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new NotFoundException("Collection not found with id: " + collectionId));

        var books = collectionBookRepository.findByCollectionId(collectionId);
        var bookDtos = collectionBookMapper.toDto(books);

        var detailsDto = collectionMapper.toDetailsDto(collection);
        detailsDto.setBooks(bookDtos);

        return detailsDto;
    }

    @Transactional
    public BasicCollectionDto createCollection(CreateCollectionRequest dto, Integer userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        var newCollection = collectionMapper.toEntity(dto);
        newCollection.setUser(user);

        if (dto.getParentId() != null) {
            var parent = collectionRepository.findByIdAndUserId(dto.getParentId(), userId)
                    .orElseThrow(() -> new NotFoundException("Parent collection not found with id: " + dto.getParentId()));

            if (getDepthFromRoot(parent) >= 4) {
                throw new IllegalArgumentException("Maximum collection depth of 4 would be exceeded.");
            }
            parent.addChildrenCollection(newCollection);
        }

        var savedCollection = collectionRepository.save(newCollection);
        return collectionMapper.toBasicDto(savedCollection);
    }

    @Transactional
    public BasicCollectionDto updateCollection(Integer collectionId, UpdateCollectionDto dto, Integer userId) {
        var collection = collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new NotFoundException("Collection not found with id: " + collectionId));

        collectionMapper.updateFromDto(dto, collection);
        var savedCollection = collectionRepository.save(collection);

        return collectionMapper.toBasicDto(savedCollection);
    }

    @Transactional
    public void moveCollection(Integer collectionId, Integer newParentId, Integer userId) {
        if (Objects.equals(collectionId, newParentId))
            throw new IllegalArgumentException("A collection cannot be its own parent.");

        var collectionToMove = collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new NotFoundException("Collection to move not found with id: " + collectionId));

        Collection newParent = null;
        if (newParentId != null) {
            newParent = collectionRepository.findByIdAndUserId(newParentId, userId)
                    .orElseThrow(() -> new NotFoundException("New parent collection not found with id: " + newParentId));
            validateMove(collectionToMove, newParent);
        }

        var oldParent = collectionToMove.getParent();
        if (oldParent != null) {
            oldParent.removeChildrenCollection(collectionToMove);
        }

        collectionToMove.setParent(newParent);
        collectionRepository.save(collectionToMove);
    }

    @Transactional
    public void bulkMoveCollections(List<Integer> collectionIds, Integer newParentId, Integer userId) {
        if (collectionIds == null || collectionIds.isEmpty())
            return;

        Collection newParent = null;
        if (newParentId != null) {
            newParent = collectionRepository.findByIdAndUserId(newParentId, userId)
                    .orElseThrow(() -> new NotFoundException("Target parent collection not found with id: " + newParentId));
        }

        var collectionsToMove = collectionRepository.findAllByIdInAndUserId(collectionIds, userId);
        if (collectionsToMove.size() != collectionIds.size())
            throw new NotFoundException("One or more collections to move were not found.");

        for (var collection : collectionsToMove) {
            if (newParent != null) {
                validateMove(collection, newParent);
            }
            var oldParent = collection.getParent();
            if (oldParent != null) {
                oldParent.removeChildrenCollection(collection);
            }
            collection.setParent(newParent);
        }

        collectionRepository.saveAll(collectionsToMove);
    }

    @Transactional
    public void deleteCollection(Integer collectionId, Integer userId) {
        var collection = collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new NotFoundException("Collection not found with id: " + collectionId));

        collectionRepository.delete(collection);
    }

    @Transactional
    public void addBookToCollections(Integer libraryBookId, List<Integer> collectionIds, Integer userId) {
        var book = libraryBookRepository.findByIdAndUserId(libraryBookId, userId)
                .orElseThrow(() -> new NotFoundException("Library book not found with id: " + libraryBookId));

        var collections = collectionRepository.findAllByIdInAndUserId(collectionIds, userId);
        if (collections.size() != collectionIds.size()) {
            throw new NotFoundException("One or more collections were not found.");
        }

        var existingMappings = collectionBookRepository.findAllByLibraryBookIdAndCollectionIdIn(libraryBookId, collectionIds)
                .stream()
                .map(cb -> cb.getCollection().getId())
                .collect(Collectors.toSet());

        var newMappings = collections.stream()
                .filter(collection -> !existingMappings.contains(collection.getId()))
                .map(collection -> CollectionBook.builder()
                        .id(new CollectionBookId(libraryBookId, collection.getId()))
                        .libraryBook(book)
                        .collection(collection)
                        .build())
                .toList();

        if (!newMappings.isEmpty()) {
            collectionBookRepository.saveAll(newMappings);
        }
    }

    @Transactional
    public void moveBook(Integer sourceCollectionId, Integer targetCollectionId, Integer libraryBookId, Integer userId) {
        libraryBookRepository.findByIdAndUserId(libraryBookId, userId)
                .orElseThrow(() -> new NotFoundException("Library book not found with id: " + libraryBookId));

        var sourceCollection = collectionRepository.findByIdAndUserId(sourceCollectionId, userId)
                .orElseThrow(() -> new NotFoundException("Source collection not found with id: " + sourceCollectionId));
        var targetCollection = collectionRepository.findByIdAndUserId(targetCollectionId, userId)
                .orElseThrow(() -> new NotFoundException("Target collection not found with id: " + targetCollectionId));

        var sourceId = new CollectionBookId(libraryBookId, sourceCollection.getId());
        var targetId = new CollectionBookId(libraryBookId, targetCollection.getId());

        if (!collectionBookRepository.existsById(sourceId)) {
            throw new NotFoundException("Book is not in the source collection.");
        }

        collectionBookRepository.deleteById(sourceId);

        if (!collectionBookRepository.existsById(targetId)) {
            var bookRef = libraryBookRepository.getReferenceById(libraryBookId);
            var collectionRef = collectionRepository.getReferenceById(targetCollectionId);
            var newMapping = CollectionBook.builder()
                    .id(targetId)
                    .libraryBook(bookRef)
                    .collection(collectionRef)
                    .build();
            collectionBookRepository.save(newMapping);
        }
    }

    private void validateMove(Collection toMove, Collection newParent) {
        checkForCircularDependency(toMove, newParent);

        if (getDepthFromRoot(newParent) + getSubtreeDepth(toMove) > 4)
            throw new IllegalArgumentException("Hierarchy would exceed maximum depth of 4.");
    }

    private void checkForCircularDependency(Collection toMove, Collection newParent) {
        var current = newParent;
        while (current != null) {
            if (current.equals(toMove))
                throw new BadRequestException("Circular dependency detected: cannot move a collection into its own sub-collection.");

            current = current.getParent();
        }
    }

    private int getDepthFromRoot(Collection node) {
        int depth = 1;
        for (var current = node.getParent(); current != null; current = current.getParent()) {
            depth++;
        }
        return depth;
    }

    private int getSubtreeDepth(Collection node) {
        if (node.getChildren() == null || node.getChildren().isEmpty())
            return 1;

        return 1 + node.getChildren().stream()
                .mapToInt(this::getSubtreeDepth)
                .max()
                .orElse(0);
    }

}

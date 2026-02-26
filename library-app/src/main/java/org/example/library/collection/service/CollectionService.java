
package org.example.library.collection.service;

import lombok.RequiredArgsConstructor;
import org.example.library.collection.domain.Collection;
import org.example.library.collection.dto.*;
import org.example.library.collection.mapper.CollectionMapper;
import org.example.library.collection.repository.CollectionRepository;
import org.example.library.collection.repository.CollectionSpecification;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.example.library.collection_book.repository.CollectionBookRepository;
import org.example.library.exception.BadRequestException;
import org.example.library.exception.NotFoundException;
import org.example.library.library_book.repository.LibraryBookRepository;
import org.example.library.user.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private static final int MAX_ALLOWED_DEPTH = 4;


    private final CollectionRepository collectionRepository;
    private final CollectionBookRepository collectionBookRepository;
    private final LibraryBookRepository libraryBookRepository;
    private final UserRepository userRepository;
    private final CollectionMapper collectionMapper;


    @Transactional(readOnly = true)
    public List<BasicCollectionDto> getAllCollections(Integer userId, Integer libraryBookId) {
        Specification<Collection> spec = CollectionSpecification.withUserIdAndOptionalLibraryBookId(userId, libraryBookId);
        return collectionMapper.toBasicDto(collectionRepository.findAll(spec));
    }

    @Transactional(readOnly = true)
    public List<BasicCollectionDto> getAllByUserIdAndBookId(Integer userId, Integer bookId) {
        return collectionMapper.toBasicDto(collectionRepository.findAllByUserIdAndBookId(userId, bookId));
    }

    @Transactional(readOnly = true)
    public Collection getExistingById(Integer id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("error.collection.not_found"));
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
                .orElseThrow(() -> new NotFoundException("error.collection.not_found"));

        var detailsDto = collectionMapper.toDetailsDto(collection);
        detailsDto.setAncestors(getAncestors(collection));

        return detailsDto;
    }

    @Transactional
    public BasicCollectionDto createCollection(CreateCollectionRequest dto, Integer userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("error.user.not_found"));

        var newCollection = collectionMapper.toEntity(dto);
        newCollection.setUser(user);

        if (dto.getParentId() != null) {
            var parent = collectionRepository.findByIdAndUserId(dto.getParentId(), userId)
                    .orElseThrow(() -> new NotFoundException("error.collection.parent_not_found"));

            if (getDepthFromRoot(parent) >= MAX_ALLOWED_DEPTH) {
                throw new BadRequestException("error.collection.max_depth_exceeded");
            }
            parent.addChildrenCollection(newCollection);
        }

        var savedCollection = collectionRepository.save(newCollection);
        return collectionMapper.toBasicDto(savedCollection);
    }

    @Transactional
    public BasicCollectionDto updateCollection(Integer collectionId, UpdateCollectionDto dto, Integer userId) {
        var collection = collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new NotFoundException("error.collection.not_found"));

        collectionMapper.updateFromDto(dto, collection);
        var savedCollection = collectionRepository.save(collection);

        return collectionMapper.toBasicDto(savedCollection);
    }

    @Transactional
    public void moveCollection(Integer collectionId, Integer newParentId, Integer userId) {
        if (Objects.equals(collectionId, newParentId))
            throw new BadRequestException("error.collection.cannot_be_own_parent");

        var collectionToMove = collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new NotFoundException("error.collection.not_found"));

        Collection newParent = null;
        if (newParentId != null) {
            newParent = collectionRepository.findByIdAndUserId(newParentId, userId)
                    .orElseThrow(() -> new NotFoundException("error.collection.not_found"));
            validateMove(collectionToMove, newParent);
        }

        var oldParent = collectionToMove.getParent();
        if (oldParent != null) {
            oldParent.removeChildrenCollection(collectionToMove);
        }

        if (newParent != null) {
            newParent.addChildrenCollection(collectionToMove);
        } else {
            collectionToMove.setParent(null);
        }

        collectionRepository.save(collectionToMove);
    }

    @Transactional
    public void bulkMoveCollections(List<Integer> collectionIds, Integer newParentId, Integer userId) {
        if (collectionIds == null || collectionIds.isEmpty())
            return;

        Collection newParent = null;
        if (newParentId != null) {
            newParent = collectionRepository.findByIdAndUserId(newParentId, userId)
                    .orElseThrow(() -> new NotFoundException("error.collection.not_found"));
        }

        var collectionsToMove = collectionRepository.findAllByIdInAndUserId(collectionIds, userId);
        if (collectionsToMove.size() != collectionIds.size())
            throw new NotFoundException("error.collection.not_found");

        for (var collection : collectionsToMove) {
            if (newParent != null) {
                validateMove(collection, newParent);
            }
            var oldParent = collection.getParent();
            if (oldParent != null) {
                oldParent.removeChildrenCollection(collection);
            }
            if (newParent != null) {
                newParent.addChildrenCollection(collection);
            } else {
                collection.setParent(null);
            }
        }

        collectionRepository.saveAll(collectionsToMove);
    }

    @Transactional
    public void deleteCollection(Integer collectionId, Integer userId) {
        var collection = collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new NotFoundException("error.collection.not_found"));

        collectionRepository.delete(collection);
    }

    @Transactional
    public void addBookToCollections(Integer libraryBookId, List<Integer> collectionIds, Integer userId) {
        var book = libraryBookRepository.findByIdAndUserId(libraryBookId, userId)
                .orElseThrow(() -> new NotFoundException("error.library_book.not_found"));

        var collections = collectionRepository.findAllByIdInAndUserId(collectionIds, userId);
        if (collections.size() != collectionIds.size()) {
            throw new NotFoundException("error.collection.not_found");
        }

        var existingMappings = collectionBookRepository.findAllByLibraryBookIdAndCollectionIdIn(libraryBookId, collectionIds)
                .stream()
                .map(cb -> cb.getCollection().getId())
                .collect(Collectors.toSet());

        var newMappings = collections.stream()
                .filter(collection -> !existingMappings.contains(collection.getId()))
                .map(collection -> CollectionBook.builder()
                        .id(new CollectionBookId(collection.getId(), libraryBookId))
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
                .orElseThrow(() -> new NotFoundException("error.library_book.not_found"));

        var sourceCollection = collectionRepository.findByIdAndUserId(sourceCollectionId, userId)
                .orElseThrow(() -> new NotFoundException("error.collection.not_found"));
        var targetCollection = collectionRepository.findByIdAndUserId(targetCollectionId, userId)
                .orElseThrow(() -> new NotFoundException("error.collection.not_found"));

        var sourceId = new CollectionBookId(libraryBookId, sourceCollection.getId());
        var targetId = new CollectionBookId(libraryBookId, targetCollection.getId());

        if (!collectionBookRepository.existsById(sourceId)) {
            throw new NotFoundException("error.collection.book_not_in_source");
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

    private List<BasicCollectionDto> getAncestors(Collection collection) {
        List<BasicCollectionDto> ancestors = new ArrayList<>(MAX_ALLOWED_DEPTH - 1);
        Collection current = collection.getParent();
        while (current != null) {
            ancestors.add(0, collectionMapper.toBasicDto(current));
            current = current.getParent();
        }
        return ancestors;
    }

    private void validateMove(Collection toMove, Collection newParent) {
        checkForCircularDependency(toMove, newParent);

        if (getDepthFromRoot(newParent) + getSubtreeDepth(toMove) > MAX_ALLOWED_DEPTH)
            throw new BadRequestException("error.collection.max_depth_exceeded");
    }

    private void checkForCircularDependency(Collection toMove, Collection newParent) {
        var current = newParent;
        while (current != null) {
            if (current.equals(toMove))
                throw new BadRequestException("error.collection.circular_dependency");

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

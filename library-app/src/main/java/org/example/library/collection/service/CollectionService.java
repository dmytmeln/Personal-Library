package org.example.library.collection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.collection.domain.Collection;
import org.example.library.collection.dto.BasicCollectionDto;
import org.example.library.collection.dto.CollectionDetailsDto;
import org.example.library.collection.dto.CollectionNodeDto;
import org.example.library.collection.dto.CollectionValidationProjection;
import org.example.library.collection.dto.CreateCollectionRequest;
import org.example.library.collection.dto.UpdateCollectionDto;
import org.example.library.collection.mapper.CollectionHierarchyAssembler;
import org.example.library.collection.mapper.CollectionMapper;
import org.example.library.collection.repository.CollectionRepository;
import org.example.library.collection.repository.CollectionSpecification;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.example.library.collection_book.repository.CollectionBookRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.example.library.library_book.repository.LibraryBookRepository;
import org.example.library.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionService {

    private static final String ERROR_COLLECTION_NOT_FOUND_MSG = "error.collection.not_found";
    private static final String ERROR_PARENT_NOT_FOUND_MSG = "error.collection.parent_not_found";
    private static final String ERROR_MAX_HIERARCHY_LEVEL_EXCEEDED_MSG = "error.collection.max_hierarchy_level_exceeded";
    private static final String ERROR_CIRCULAR_DEPENDENCY_MSG = "error.collection.circular_dependency";
    private static final String ERROR_CANNOT_BE_OWN_PARENT_MSG = "error.collection.cannot_be_own_parent";
    private static final String ERROR_BOOK_NOT_IN_SOURCE_MSG = "error.collection.book_not_in_source";
    private static final String ERROR_BOOK_ALREADY_IN_TARGET_MSG = "error.collection.book_already_in_target";
    private static final String ERROR_LIBRARY_BOOK_NOT_FOUND_MSG = "error.library_book.not_found";
    private static final int MAX_ALLOWED_HIERARCHY_LEVELS = 4;

    private final CollectionRepository collectionRepository;
    private final CollectionBookRepository collectionBookRepository;
    private final LibraryBookRepository libraryBookRepository;
    private final UserRepository userRepository;
    private final CollectionMapper collectionMapper;
    private final CollectionHierarchyAssembler hierarchyAssembler = new CollectionHierarchyAssembler();

    @Transactional(readOnly = true)
    public List<BasicCollectionDto> getCollectionsContainingLibraryBook(Integer userId, Integer libraryBookId) {
        var spec = CollectionSpecification.withUserIdAndOptionalLibraryBookId(userId, libraryBookId);
        var collections = collectionRepository.findAll(spec);

        return collectionMapper.toBasicDto(collections);
    }

    @Transactional(readOnly = true)
    public List<CollectionNodeDto> getUserCollectionHierarchy(Integer userId) {
        var projections = collectionRepository.findCollectionHierarchyProjectionsByUserId(userId);
        var nodes = collectionMapper.toNodeDto(projections);

        return hierarchyAssembler.assemble(nodes);
    }

    @Transactional(readOnly = true)
    public CollectionDetailsDto getCollectionDetails(Integer collectionId, Integer userId) {
        var collection = findOwnedCollectionWithChildren(collectionId, userId);
        var detailsDto = collectionMapper.toDetailsDto(collection);

        var ancestors = collectionRepository.findAncestors(collectionId).stream()
                .map(collectionMapper::toBasicDto)
                .toList();
        detailsDto.setAncestors(ancestors);

        return detailsDto;
    }

    @Transactional
    public BasicCollectionDto createCollection(CreateCollectionRequest dto, Integer userId) {
        var newCollection = collectionMapper.toEntity(dto);
        var userReference = userRepository.getReferenceById(userId);
        newCollection.setUser(userReference);

        var hasParent = dto.getParentId() != null;
        if (hasParent) {
            attachToParent(newCollection, dto.getParentId(), userId);
        }

        var savedCollection = collectionRepository.save(newCollection);
        log.info("[COLLECTION_CREATE] User ID: {}, Collection ID: {}", userId, savedCollection.getId());

        return collectionMapper.toBasicDto(savedCollection);
    }

    @Transactional
    public BasicCollectionDto updateCollection(Integer collectionId, UpdateCollectionDto dto, Integer userId) {
        var collection = findOwnedCollection(collectionId, userId);

        collectionMapper.updateFromDto(dto, collection);
        var savedCollection = collectionRepository.save(collection);
        log.info("[COLLECTION_UPDATE] User ID: {}, Collection ID: {}", userId, collectionId);

        return collectionMapper.toBasicDto(savedCollection);
    }

    @Transactional
    public void moveCollection(Integer collectionId, Integer newParentId, Integer userId) {
        if (Objects.equals(collectionId, newParentId)) {
            throw new BadRequestException(ERROR_CANNOT_BE_OWN_PARENT_MSG);
        }

        var movingToTopLevel = newParentId == null;
        var collectionToMove = findOwnedCollection(collectionId, userId);

        if (movingToTopLevel) {
            collectionToMove.setParent(null);
        } else {
            moveUnderNewParent(collectionToMove, newParentId, userId);
        }

        collectionRepository.save(collectionToMove);
        log.info("[COLLECTION_MOVE] User ID: {}, Collection ID: {}, New Parent ID: {}", userId, collectionId, newParentId);
    }

    @Transactional
    public void deleteCollection(Integer collectionId, Integer userId) {
        var deletedCount = collectionRepository.deleteById(collectionId, userId);
        if (deletedCount == 0) {
            throw new NotFoundException(ERROR_COLLECTION_NOT_FOUND_MSG);
        }

        log.info("[COLLECTION_DELETE] User ID: {}, Collection ID: {}", userId, collectionId);
    }

    @Transactional
    public void moveBook(Integer sourceCollectionId, Integer targetCollectionId, Integer libraryBookId, Integer userId) {
        var movingBookWithinSameCollection = Objects.equals(sourceCollectionId, targetCollectionId);
        if (movingBookWithinSameCollection) {
            return;
        }

        findOwnedCollection(sourceCollectionId, userId);
        var targetCollection = findOwnedCollection(targetCollectionId, userId);

        var targetMappingId = new CollectionBookId(targetCollectionId, libraryBookId);
        validateBookMove(sourceCollectionId, targetMappingId, libraryBookId, userId);

        collectionBookRepository.deleteByLibraryBookIdAndCollectionId(libraryBookId, sourceCollectionId);

        saveToTargetCollection(targetCollection, targetMappingId);
        log.info("[COLLECTION_BOOK_MOVE] User ID: {}, Library Book ID: {}, Source Collection ID: {}, Target Collection ID: {}",
                userId, libraryBookId, sourceCollectionId, targetCollectionId);
    }

    private Collection findOwnedCollection(Integer collectionId, Integer userId) {
        return collectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new NotFoundException(ERROR_COLLECTION_NOT_FOUND_MSG));
    }

    private Collection findOwnedCollectionWithChildren(Integer collectionId, Integer userId) {
        return collectionRepository.findByIdAndUserIdWithChildren(collectionId, userId)
                .orElseThrow(() -> new NotFoundException(ERROR_COLLECTION_NOT_FOUND_MSG));
    }

    private void attachToParent(Collection childCollection, Integer parentId, Integer userId) {
        if (!collectionRepository.existsByIdAndUserId(parentId, userId)) {
            throw new NotFoundException(ERROR_PARENT_NOT_FOUND_MSG);
        }

        var parentLevel = collectionRepository.getHierarchyLevel(parentId);
        if (childWouldExceedMaxLevels(parentLevel)) {
            throw new BadRequestException(ERROR_MAX_HIERARCHY_LEVEL_EXCEEDED_MSG);
        }

        var parentReference = collectionRepository.getReferenceById(parentId);
        childCollection.setParent(parentReference);
    }

    private void moveUnderNewParent(Collection collectionToMove, Integer newParentId, Integer userId) {
        var newParent = findOwnedCollectionWithChildren(newParentId, userId);
        validateNewParent(collectionToMove, newParent);

        newParent.addChildrenCollection(collectionToMove);
    }

    private void validateNewParent(Collection collectionToMove, Collection newParent) {
        var validation = collectionRepository.getValidationData(collectionToMove.getId(), newParent.getId());

        if (validation.isCircular()) {
            throw new BadRequestException(ERROR_CIRCULAR_DEPENDENCY_MSG);
        }

        if (moveWouldExceedMaxLevels(validation)) {
            throw new BadRequestException(ERROR_MAX_HIERARCHY_LEVEL_EXCEEDED_MSG);
        }
    }

    private void validateBookMove(Integer sourceCollectionId, CollectionBookId targetMappingId, Integer libraryBookId, Integer userId) {
        if (!libraryBookRepository.existsByIdAndUserId(libraryBookId, userId)) {
            throw new NotFoundException(ERROR_LIBRARY_BOOK_NOT_FOUND_MSG);
        }

        var isBookInSourceCollection = collectionBookRepository.existsByLibraryBookIdAndCollectionId(libraryBookId, sourceCollectionId);
        if (!isBookInSourceCollection) {
            throw new NotFoundException(ERROR_BOOK_NOT_IN_SOURCE_MSG);
        }

        if (collectionBookRepository.existsById(targetMappingId)) {
            throw new BadRequestException(ERROR_BOOK_ALREADY_IN_TARGET_MSG);
        }
    }

    private void saveToTargetCollection(Collection targetCollection, CollectionBookId targetMappingId) {
        var libraryBookRef = libraryBookRepository.getReferenceById(targetMappingId.getLibraryBookId());
        var newMapping = CollectionBook.builder()
                .id(targetMappingId)
                .libraryBook(libraryBookRef)
                .collection(targetCollection)
                .build();

        collectionBookRepository.save(newMapping);
    }

    private boolean childWouldExceedMaxLevels(int parentLevel) {
        return parentLevel >= MAX_ALLOWED_HIERARCHY_LEVELS;
    }

    private boolean moveWouldExceedMaxLevels(CollectionValidationProjection validation) {
        var deepestDescendantLevel = validation.getNewParentLevel() + validation.getMovedDescendantLevels();
        return deepestDescendantLevel > MAX_ALLOWED_HIERARCHY_LEVELS;
    }

}

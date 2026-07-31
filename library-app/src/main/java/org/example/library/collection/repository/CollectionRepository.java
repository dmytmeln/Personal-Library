package org.example.library.collection.repository;

import org.example.library.collection.domain.Collection;
import org.example.library.collection.dto.CollectionHierarchyProjection;
import org.example.library.collection.dto.CollectionValidationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Integer>, JpaSpecificationExecutor<Collection> {

    @Query("""
            SELECT new org.example.library.collection.dto.CollectionHierarchyProjection(c.id, c.name, c.parent.id)
            FROM Collection c
            WHERE c.user.id = :userId
            """)
    List<CollectionHierarchyProjection> findCollectionHierarchyProjectionsByUserId(Integer userId);

    @Query(value = """
            WITH RECURSIVE collection_path AS (
                SELECT *, 1 as depth
                FROM collections
                WHERE collection_id = (SELECT parent_id FROM collections WHERE collection_id = :id)

                UNION ALL

                SELECT c.*, cp.depth + 1
                FROM collections c
                JOIN collection_path cp ON c.collection_id = cp.parent_id
            )
            SELECT collection_id, user_id, name, description, created_at, updated_at, parent_id
            FROM collection_path
            ORDER BY depth DESC
            """, nativeQuery = true)
    List<Collection> findAncestors(Integer id);

    @Query(value = """
            WITH RECURSIVE
            descendants AS (
                SELECT collection_id, 1 AS level FROM collections WHERE collection_id = :toMoveId
                UNION ALL
                SELECT c.collection_id, d.level + 1 FROM collections c
                JOIN descendants d ON c.parent_id = d.collection_id
            ),
            ancestors AS (
                SELECT collection_id, parent_id, 1 AS level FROM collections WHERE collection_id = :newParentId
                UNION ALL
                SELECT c.collection_id, c.parent_id, a.level + 1 FROM collections c
                JOIN ancestors a ON c.collection_id = a.parent_id
            )
            SELECT
                (SELECT COALESCE(MAX(level), 0) FROM descendants) AS movedDescendantLevels,
                (SELECT COALESCE(MAX(level), 0) FROM ancestors) AS newParentLevel,
                (SELECT EXISTS (SELECT 1 FROM ancestors WHERE collection_id = :toMoveId)) AS circular
            """, nativeQuery = true)
    CollectionValidationProjection getValidationData(Integer toMoveId, Integer newParentId);

    @Query(value = """
            WITH RECURSIVE path AS (
                SELECT parent_id, 1 as level FROM collections WHERE collection_id = :id
                UNION ALL
                SELECT c.parent_id, p.level + 1 FROM collections c
                JOIN path p ON c.collection_id = p.parent_id
            )
            SELECT COALESCE(MAX(level), 0) FROM path
            """, nativeQuery = true)
    int getHierarchyLevel(Integer id);

    @Query("SELECT c FROM Collection c LEFT JOIN FETCH c.children WHERE c.id = :id AND c.user.id = :userId")
    Optional<Collection> findByIdAndUserIdWithChildren(Integer id, Integer userId);

    Optional<Collection> findByIdAndUserId(Integer id, Integer userId);

    boolean existsByIdAndUserId(Integer collectionId, Integer userId);

    @Modifying
    @Query("DELETE FROM Collection c WHERE c.id = :id AND c.user.id = :userId")
    int deleteById(Integer id, Integer userId);

}

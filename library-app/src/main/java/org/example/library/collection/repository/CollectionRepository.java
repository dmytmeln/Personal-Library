package org.example.library.collection.repository;

import org.example.library.collection.domain.Collection;
import org.example.library.collection.dto.CollectionValidationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Integer>, JpaSpecificationExecutor<Collection> {

    List<Collection> findAllByUserId(Integer userId);

    @Query(value = """
            SELECT c.collection_id, c.user_id, c.parent_id, c.name, c.description, c.color, c.created_at, c.updated_at
            FROM collections c
            JOIN collection_books cb ON cb.collection_id = c.collection_id
            JOIN library_books lb ON lb.library_book_id = cb.library_book_id
            WHERE lb.user_id = :userId AND lb.book_id = :bookId
            """, nativeQuery = true)
    List<Collection> findAllByUserIdAndBookId(Integer userId, Integer bookId);

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
            SELECT collection_id, user_id, name, description, color, created_at, updated_at, parent_id
            FROM collection_path
            ORDER BY depth DESC
            """, nativeQuery = true)
    List<Collection> findAncestors(Integer id);

    @Query(value = """
            WITH RECURSIVE
            subtree AS (
                SELECT collection_id, 1 AS level FROM collections WHERE collection_id = :toMoveId
                UNION ALL
                SELECT c.collection_id, s.level + 1 FROM collections c
                JOIN subtree s ON c.parent_id = s.collection_id
            ),
            parent_path AS (
                SELECT collection_id, parent_id, 1 AS level FROM collections WHERE collection_id = :newParentId
                UNION ALL
                SELECT c.collection_id, c.parent_id, p.level + 1 FROM collections c
                JOIN parent_path p ON c.collection_id = p.parent_id
            )
            SELECT
                (SELECT COALESCE(MAX(level), 0) FROM subtree) AS subtreeDepth,
                (SELECT COALESCE(MAX(level), 0) FROM parent_path) AS parentRootDepth,
                (SELECT EXISTS (SELECT 1 FROM parent_path WHERE collection_id = :toMoveId)) AS isCircular
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
    int getDepth(Integer id);

    @Query("SELECT COUNT(c) FROM Collection c WHERE c.user.id = :userId AND c.id IN (:sourceId, :targetId)")
    long countByUserIdAndIds(Integer userId, Integer sourceId, Integer targetId);

    @Query("SELECT c FROM Collection c LEFT JOIN FETCH c.children WHERE c.id = :id AND c.user.id = :userId")
    Optional<Collection> findByIdAndUserIdWithChildren(Integer id, Integer userId);

    Optional<Collection> findByIdAndUserId(Integer id, Integer userId);

    boolean existsByIdAndUserId(Integer collectionId, Integer userId);

    @Modifying
    @Query("DELETE FROM Collection c WHERE c.id = :id AND c.user.id = :userId")
    int deleteById(Integer id, Integer userId);

}

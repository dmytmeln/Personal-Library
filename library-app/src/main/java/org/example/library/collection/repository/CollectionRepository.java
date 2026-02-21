package org.example.library.collection.repository;

import org.example.library.collection.domain.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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

    Optional<Collection> findByIdAndUserId(Integer id, Integer userId);

    List<Collection> findAllByIdInAndUserId(List<Integer> ids, Integer userId);

}

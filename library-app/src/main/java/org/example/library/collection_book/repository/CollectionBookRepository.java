package org.example.library.collection_book.repository;

import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CollectionBookRepository extends JpaRepository<CollectionBook, CollectionBookId> {

    @EntityGraph(attributePaths = {"libraryBookView"})
    Optional<CollectionBook> findWithViewById(CollectionBookId id);

    @EntityGraph(attributePaths = {"libraryBookView"})
    List<CollectionBook> findByIdCollectionId(Integer collectionId);

    List<CollectionBook> findAllByLibraryBookIdAndCollectionIdIn(Integer libraryBookId, List<Integer> collectionIds);

    @Modifying
    @Query("DELETE FROM CollectionBook cb WHERE cb.id.libraryBookId = :libraryBookId AND cb.libraryBook.user.id = :userId")
    void deleteByLibraryBookIdAndUserId(@Param("libraryBookId") Integer libraryBookId, @Param("userId") Integer userId);

    @Modifying
    @Query("DELETE FROM CollectionBook cb WHERE cb.id.libraryBookId IN :libraryBookIds AND cb.libraryBook.user.id = :userId")
    void deleteAllByLibraryBookIdInAndUserId(List<Integer> libraryBookIds, Integer userId);

    @Modifying
    @Query("DELETE FROM CollectionBook cb WHERE cb.id.collectionId = :collectionId AND cb.id.libraryBookId IN :libraryBookIds AND cb.collection.user.id = :userId")
    void deleteAllByCollectionIdAndLibraryBookIdInAndUserId(Integer collectionId, List<Integer> libraryBookIds, Integer userId);

    @Query("SELECT cb.id.libraryBookId FROM CollectionBook cb WHERE cb.id.collectionId = :collectionId")
    Set<Integer> findLibraryBookIdsByCollectionId(Integer collectionId);

}

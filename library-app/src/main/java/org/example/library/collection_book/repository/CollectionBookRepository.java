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

public interface CollectionBookRepository extends JpaRepository<CollectionBook, CollectionBookId> {

    @EntityGraph(attributePaths = {"libraryBookView"})
    Optional<CollectionBook> findWithViewById(CollectionBookId id);

    @EntityGraph(attributePaths = {"libraryBookView"})
    List<CollectionBook> findByIdCollectionId(Integer collectionId);

    List<CollectionBook> findAllByLibraryBookIdAndCollectionIdIn(Integer libraryBookId, List<Integer> collectionIds);

    @Modifying
    @Query("DELETE FROM CollectionBook cb WHERE cb.id.libraryBookId = :libraryBookId AND cb.libraryBook.user.id = :userId")
    void deleteByLibraryBookIdAndUserId(@Param("libraryBookId") Integer libraryBookId, @Param("userId") Integer userId);

}

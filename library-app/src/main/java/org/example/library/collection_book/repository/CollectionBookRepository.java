package org.example.library.collection_book.repository;

import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.example.library.library_book.domain.LibraryBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface CollectionBookRepository extends JpaRepository<CollectionBook, CollectionBookId> {

    List<CollectionBook> findByIdCollectionId(Integer collectionId);

    void deleteByLibraryBook(LibraryBook libraryBook);

    List<CollectionBook> findByCollectionId(Integer collectionId);

    List<CollectionBook> findAllByLibraryBookIdAndCollectionIdIn(Integer libraryBookId, List<Integer> collectionIds);

}

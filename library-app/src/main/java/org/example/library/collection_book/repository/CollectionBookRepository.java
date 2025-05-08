package org.example.library.collection_book.repository;

import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionBookRepository extends JpaRepository<CollectionBook, CollectionBookId> {

    List<CollectionBook> findByIdCollectionId(Integer collectionId);

}

package org.example.library.collectionBook.repository;

import org.example.library.collectionBook.domain.CollectionBook;
import org.example.library.collectionBook.domain.CollectionBookId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionBookRepository extends JpaRepository<CollectionBook, CollectionBookId> {

    List<CollectionBook> findByIdCollectionId(Integer collectionId);

}

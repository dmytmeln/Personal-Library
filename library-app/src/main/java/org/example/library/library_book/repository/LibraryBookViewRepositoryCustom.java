package org.example.library.library_book.repository;

import org.example.library.collection_book.dto.CollectionBookSearchParams;
import org.example.library.library_book.domain.LibraryBookView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LibraryBookViewRepositoryCustom {
    Page<LibraryBookView> findCollectionBooks(Integer userId, Integer collectionId, CollectionBookSearchParams searchParams, Pageable pageable);
}

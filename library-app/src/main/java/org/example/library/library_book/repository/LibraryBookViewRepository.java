package org.example.library.library_book.repository;

import org.example.library.library_book.domain.LibraryBookView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibraryBookViewRepository extends JpaRepository<LibraryBookView, Integer>, JpaSpecificationExecutor<LibraryBookView>, LibraryBookViewRepositoryCustom {

    Page<LibraryBookView> findAllByUserIdAndLanguageCode(Integer userId, String languageCode, Pageable pageable);

    @EntityGraph(attributePaths = {"authors"})
    Optional<LibraryBookView> findByIdAndLanguageCode(Integer id, String languageCode);

    @EntityGraph(attributePaths = {"authors"})
    Optional<LibraryBookView> findByBookIdAndUserIdAndLanguageCode(Integer bookId, Integer userId, String languageCode);

}

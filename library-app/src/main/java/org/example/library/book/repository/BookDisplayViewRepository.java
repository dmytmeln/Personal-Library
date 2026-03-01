package org.example.library.book.repository;

import org.example.library.book.domain.BookDisplayView;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookDisplayViewRepository extends JpaRepository<BookDisplayView, Integer>, JpaSpecificationExecutor<BookDisplayView> {

    @EntityGraph(attributePaths = {"authors"})
    Optional<BookDisplayView> findByIdAndLanguageCode(Integer id, String languageCode);

}

package org.example.library.book.repository;

import org.example.library.book.domain.Book;
import org.example.library.book.dto.LanguageWithCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    @Query("""
            SELECT
                tr.bookLanguage AS language,
                COUNT(b) AS count
            FROM Book b
            JOIN b.translations tr ON tr.languageCode = :lang
            GROUP BY tr.bookLanguage
            ORDER BY COUNT(b) DESC
            """)
    List<LanguageWithCount> findAllLanguagesWithCount(String lang);

    @Override
    @EntityGraph(attributePaths = {"category"}, type = EntityGraph.EntityGraphType.LOAD)
    Page<Book> findAll(@Nullable Specification<Book> spec, Pageable pageable);

}

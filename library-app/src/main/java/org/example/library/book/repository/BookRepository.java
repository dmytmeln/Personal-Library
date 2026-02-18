package org.example.library.book.repository;

import org.example.library.book.domain.Book;
import org.example.library.book.dto.LanguageWithCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    @Query("""
            SELECT
                b.language AS language,
                COUNT(b) AS count
            FROM Book b
            GROUP BY b.language
            ORDER BY COUNT(b) DESC
            """)
    List<LanguageWithCount> findAllLanguagesWithCount();

}

package org.example.library.author.repository;

import org.example.library.author.domain.Author;
import org.example.library.author.dto.AuthorWithBooksCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuthorRepository extends JpaRepository<Author, Integer> {

    @Query("""
            SELECT
                a.id AS id,
                a.fullName AS fullName,
                a.country AS country,
                a.birthYear AS birthYear,
                a.deathYear AS deathYear,
                COUNT(b) AS booksCount
            FROM Author a
            LEFT JOIN a.books b
            WHERE (:name IS NULL OR LOWER(a.fullName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
            GROUP BY a.id, a.fullName, a.country, a.birthYear, a.deathYear
            """)
    Page<AuthorWithBooksCount> searchWithBooksCount(String name, Pageable pageable);

}

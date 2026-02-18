package org.example.library.author.repository;

import org.example.library.author.domain.Author;
import org.example.library.author.dto.AuthorWithBooksCount;
import org.example.library.author.dto.CountryWithCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
              AND (:country IS NULL OR a.country = :country)
              AND (:birthYearMin IS NULL OR a.birthYear >= :birthYearMin)
              AND (:birthYearMax IS NULL OR a.birthYear <= :birthYearMax)
            GROUP BY a.id, a.fullName, a.country, a.birthYear, a.deathYear
            HAVING (:booksCountMin IS NULL OR COUNT(b) >= :booksCountMin)
               AND (:booksCountMax IS NULL OR COUNT(b) <= :booksCountMax)
            """)
    Page<AuthorWithBooksCount> searchWithBooksCount(
            @Param("name") String name,
            @Param("country") String country,
            @Param("birthYearMin") Short birthYearMin,
            @Param("birthYearMax") Short birthYearMax,
            @Param("booksCountMin") Integer booksCountMin,
            @Param("booksCountMax") Integer booksCountMax,
            Pageable pageable
    );

    @Query("""
            SELECT
                a.country AS country,
                COUNT(a) AS count
            FROM Author a
            GROUP BY a.country
            ORDER BY COUNT(a) DESC
            """)
    List<CountryWithCount> findAllCountriesWithCount();

}

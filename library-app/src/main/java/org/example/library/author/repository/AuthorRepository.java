package org.example.library.author.repository;

import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorDisplayView;
import org.example.library.author.dto.AuthorSearchParams;
import org.example.library.author.dto.AuthorWithBooksCount;
import org.example.library.author.dto.CountryWithCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Integer> {

    @Query("SELECT v FROM AuthorDisplayView v WHERE v.id = :id AND v.languageCode = :languageCode")
    Optional<AuthorDisplayView> findDisplayViewByIdAndLanguageCode(Integer id, String languageCode);

    @Query("""
            SELECT
                a.id AS id,
                tr.fullName AS fullName,
                tr.country AS country,
                a.birthYear AS birthYear,
                a.deathYear AS deathYear,
                a.popularityCount AS popularityCount,
                COUNT(b) AS booksCount
            FROM Author a
            JOIN a.translations tr ON tr.languageCode = :lang
            LEFT JOIN a.books b
            WHERE (:#{#searchParams.name} IS NULL OR (LOWER(tr.fullName) LIKE LOWER(CONCAT('%', CAST(:#{#searchParams.name} AS string), '%'))
               OR FUNCTION('similarity', tr.fullName, CAST(:#{#searchParams.name} AS string)) > 0.3))
              AND (:#{#searchParams.country} IS NULL OR LOWER(tr.country) = LOWER(CAST(:#{#searchParams.country} AS string)))
              AND (:#{#searchParams.birthYearMin} IS NULL OR a.birthYear >= :#{#searchParams.birthYearMin})
              AND (:#{#searchParams.birthYearMax} IS NULL OR a.birthYear <= :#{#searchParams.birthYearMax})
            GROUP BY a.id, tr.fullName, tr.country, a.birthYear, a.deathYear
            HAVING (:#{#searchParams.booksCountMin} IS NULL OR COUNT(b) >= :#{#searchParams.booksCountMin})
               AND (:#{#searchParams.booksCountMax} IS NULL OR COUNT(b) <= :#{#searchParams.booksCountMax})
            """)
    Page<AuthorWithBooksCount> searchWithBooksCount(@Param("searchParams") AuthorSearchParams searchParams,
                                                    @Param("lang") String lang,
                                                    Pageable pageable);

    @Query("""
            SELECT
                tr.country AS country,
                COUNT(a) AS count
            FROM Author a
            JOIN a.translations tr ON tr.languageCode = :lang
            GROUP BY tr.country
            ORDER BY COUNT(a) DESC
            """)
    List<CountryWithCount> findAllCountriesWithCount(String lang);

    @Query("""
            SELECT
                a.id AS id,
                tr.fullName AS fullName,
                tr.country AS country,
                a.birthYear AS birthYear,
                a.deathYear AS deathYear,
                a.popularityCount AS popularityCount,
                COUNT(DISTINCT lb.id) AS booksCount
            FROM Author a
            JOIN a.translations tr ON tr.languageCode = :lang
            JOIN a.books b
            JOIN LibraryBook lb ON lb.book.id = b.id
            WHERE lb.user.id = :userId
              AND (:#{#searchParams.name} IS NULL OR (LOWER(tr.fullName) LIKE LOWER(CONCAT('%', CAST(:#{#searchParams.name} AS string), '%'))
               OR FUNCTION('similarity', tr.fullName, CAST(:#{#searchParams.name} AS string)) > 0.3))
              AND (:#{#searchParams.country} IS NULL OR LOWER(tr.country) = LOWER(CAST(:#{#searchParams.country} AS string)))
              AND (:#{#searchParams.birthYearMin} IS NULL OR a.birthYear >= :#{#searchParams.birthYearMin})
              AND (:#{#searchParams.birthYearMax} IS NULL OR a.birthYear <= :#{#searchParams.birthYearMax})
            GROUP BY a.id, tr.fullName, tr.country, a.birthYear, a.deathYear
            HAVING (:#{#searchParams.booksCountMin} IS NULL OR COUNT(DISTINCT lb.id) >= :#{#searchParams.booksCountMin})
               AND (:#{#searchParams.booksCountMax} IS NULL OR COUNT(DISTINCT lb.id) <= :#{#searchParams.booksCountMax})
            """)
    Page<AuthorWithBooksCount> searchForUser(@Param("userId") Integer userId,
                                             @Param("searchParams") AuthorSearchParams searchParams,
                                             @Param("lang") String lang,
                                             Pageable pageable);

    @Query("""
            SELECT
                tr.country AS country,
                COUNT(DISTINCT a.id) AS count
            FROM Author a
            JOIN a.translations tr ON tr.languageCode = :lang
            JOIN a.books b
            JOIN LibraryBook lb ON lb.book.id = b.id
            WHERE lb.user.id = :userId
            GROUP BY tr.country
            ORDER BY COUNT(DISTINCT a.id) DESC
            """)
    List<CountryWithCount> findAllCountriesForUser(Integer userId, String lang);

    @Modifying
    @Query(value = """
            UPDATE authors SET popularity_count = popularity_count + 1
            WHERE author_id IN (SELECT author_id FROM book_authors WHERE book_id IN :bookIds)
            """,
            nativeQuery = true)
    void incrementPopularityCountByBookIds(List<Integer> bookIds);

    @Modifying
    @Query(value = """
            UPDATE authors SET popularity_count = popularity_count - 1
            WHERE author_id IN (SELECT author_id FROM book_authors WHERE book_id IN :bookIds)
            """,
            nativeQuery = true)
    void decrementPopularityCountByBookIds(List<Integer> bookIds);

}

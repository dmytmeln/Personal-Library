package org.example.library.book.repository;

import org.example.library.book.domain.BookDisplayView;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookDisplayViewRepository extends JpaRepository<BookDisplayView, Integer>, JpaSpecificationExecutor<BookDisplayView> {

    @EntityGraph(attributePaths = {"authors"})
    Optional<BookDisplayView> findByIdAndLanguageCode(Integer id, String languageCode);

    @Query(value = """
            SELECT b.book_id, b.category_id, b.publish_year, b.pages, b.cover_image_url, 
                   bt.language_code, bt.title, bt.description, bt.book_language, ct.name as category_name
            FROM books b
            JOIN book_translations bt ON b.book_id = bt.book_id
            JOIN category_translations ct ON b.category_id = ct.category_id
                                          AND bt.language_code = ct.language_code
            LEFT JOIN library_books lb ON b.book_id = lb.book_id AND lb.user_id = :userId
            WHERE bt.language_code = :languageCode
              AND b.description_vector IS NOT NULL
              AND lb.library_book_id IS NULL
            ORDER BY b.description_vector <=> cast(:vector as vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<BookDisplayView> findSimilarBooks(float[] vector, String languageCode, Integer userId, int limit);

    @Query(value = """
            SELECT v.*
            FROM books_display_view v
            JOIN (
                SELECT book_id, COUNT(*) as popularity
                FROM library_books
                WHERE added_at >= :since
                GROUP BY book_id
            ) p ON v.book_id = p.book_id
            LEFT JOIN library_books ulb ON v.book_id = ulb.book_id AND ulb.user_id = :userId
            WHERE v.language_code = :languageCode
              AND ulb.library_book_id IS NULL
            ORDER BY COALESCE(p.popularity, 0) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<BookDisplayView> findPopularBooksRecently(String languageCode, Integer userId, LocalDateTime since, int limit);

    @Query(value = """
            SELECT v.*
            FROM books_display_view v
            LEFT JOIN library_books lb ON v.book_id = lb.book_id AND lb.user_id = :userId
            WHERE v.language_code = :languageCode
              AND v.publish_year = :year
              AND lb.library_book_id IS NULL
            ORDER BY v.book_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<BookDisplayView> findNewArrivals(String languageCode, Integer userId, short year, int limit);

}


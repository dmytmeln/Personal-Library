package org.example.library.library_book.repository;

import org.example.library.book.dto.LanguageWithCount;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.domain.LibraryBookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, Integer> {

    @Query("""
            SELECT
                lb.book.language AS language,
                COUNT(lb) AS count
            FROM LibraryBook lb
            WHERE lb.user.id = :userId
            GROUP BY lb.book.language
            ORDER BY COUNT(lb) DESC
            """)
    List<LanguageWithCount> findLanguagesWithCountByUserId(Integer userId);

    @Modifying
    @Query("DELETE FROM LibraryBook lb WHERE lb.id IN :ids AND lb.user.id = :userId")
    void deleteAllByIdInAndUserId(List<Integer> ids, Integer userId);

    @Query(value = """
            SELECT rating
            FROM library_books
            WHERE book_id = :bookId
            """, nativeQuery = true)
    List<Integer> findBookRatings(Integer bookId);

    @Query(value = """
            SELECT status
            FROM library_books
            WHERE book_id = :bookId AND user_id = :userId
            """, nativeQuery = true)
    Optional<LibraryBookStatus> findStatusByBookIdAndUserId(Integer bookId, Integer userId);

    @Query(value = """
            SELECT rating
            FROM library_books
            WHERE book_id = :bookId AND user_id = :userId
            """, nativeQuery = true)
    Optional<Integer> findUserRatingOfBook(Integer bookId, Integer userId);

    @Query("SELECT lb.book.id FROM LibraryBook lb WHERE lb.user.id = :userId AND lb.book.id IN :bookIds")
    List<Integer> findExistingBookIdsInLibrary(Integer userId, List<Integer> bookIds);


    @Modifying
    @Query("UPDATE LibraryBook lb SET lb.rating = :rating WHERE lb.id = :id AND lb.user.id = :userId")
    int updateRating(Integer id, Integer userId, Byte rating);

    @Modifying
    @Query("UPDATE LibraryBook lb SET lb.status = :status WHERE lb.id = :id AND lb.user.id = :userId")
    int updateStatus(Integer id, Integer userId, LibraryBookStatus status);

    @Modifying
    @Query("""
            UPDATE LibraryBook lb
            SET lb.title = NULL,
                lb.publishYear = NULL,
                lb.pages = NULL,
                lb.language = NULL,
                lb.description = NULL,
                lb.coverImageUrl = NULL
            WHERE lb.id = :id AND lb.user.id = :userId
            """)
    int resetOverriddenFields(Integer id, Integer userId);

    Optional<LibraryBook> findByIdAndUserId(Integer libraryBookId, Integer userId);

    boolean existsByIdAndUserId(Integer libraryBookId, Integer userId);

    void deleteByIdAndUserId(Integer libraryBookId, Integer userId);

    boolean existsByBookIdAndUserId(Integer bookId, Integer userId);

    List<LibraryBook> findAllByIdInAndUserId(List<Integer> ids, Integer userId);

}

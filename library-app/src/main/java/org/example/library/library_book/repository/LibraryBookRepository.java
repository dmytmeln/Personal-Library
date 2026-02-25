package org.example.library.library_book.repository;

import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.domain.LibraryBookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, Integer> {

    Page<LibraryBook> findAllByUserId(Integer userId, Pageable pageable);

    List<LibraryBook> findAllByUserId(Integer userId);

    Optional<LibraryBook> findByIdAndUserId(Integer libraryBookId, Integer userId);

    void deleteByIdAndUserId(Integer libraryBookId, Integer userId);

    boolean existsByBookIdAndUserId(Integer bookId, Integer userId);

    List<LibraryBook> findAllByIdInAndUserId(List<Integer> ids, Integer userId);

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

}

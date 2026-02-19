package org.example.library.library_book.repository;

import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.domain.LibraryBookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, Integer> {

    Page<LibraryBook> findAllByUserId(Integer userId, Pageable pageable);

    Optional<LibraryBook> findByIdAndUserId(Integer libraryBookId, Integer userId);

    void deleteByIdAndUserId(Integer libraryBookId, Integer userId);

    boolean existsByBookIdAndUserId(Integer bookId, Integer userId);

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

}

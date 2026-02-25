package org.example.library.note.repository;

import org.example.library.note.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Integer> {

    Optional<Note> findByLibraryBookIdAndLibraryBookUserId(Integer libraryBookId, Integer userId);

    @Modifying
    @Query("DELETE FROM Note n WHERE n.libraryBook.id = :libraryBookId AND n.libraryBook.user.id = :userId")
    void deleteByLibraryBookIdAndLibraryBookUserId(Integer libraryBookId, Integer userId);
    
}

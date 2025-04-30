package org.example.library.userBooks.repository;

import org.example.library.userBooks.domain.UserBook;
import org.example.library.userBooks.domain.UserBookId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBookRepository extends JpaRepository<UserBook, UserBookId> {

    List<UserBook> findAllByIdUserId(Integer userId);

}

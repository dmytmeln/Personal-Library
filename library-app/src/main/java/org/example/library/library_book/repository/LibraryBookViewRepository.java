package org.example.library.library_book.repository;

import org.example.library.library_book.domain.LibraryBookView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryBookViewRepository extends JpaRepository<LibraryBookView, Integer>, JpaSpecificationExecutor<LibraryBookView>, LibraryBookViewRepositoryCustom {

    Page<LibraryBookView> findAllByUserId(Integer userId, Pageable pageable);

}

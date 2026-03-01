package org.example.library.author.repository;

import org.example.library.author.domain.AuthorDisplayView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorDisplayViewRepository extends JpaRepository<AuthorDisplayView, Integer>, JpaSpecificationExecutor<AuthorDisplayView> {

    Optional<AuthorDisplayView> findByIdAndLanguageCode(Integer id, String languageCode);

}

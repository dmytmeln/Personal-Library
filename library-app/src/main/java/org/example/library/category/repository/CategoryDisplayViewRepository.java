package org.example.library.category.repository;

import org.example.library.category.domain.CategoryDisplayView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryDisplayViewRepository extends JpaRepository<CategoryDisplayView, Integer>, JpaSpecificationExecutor<CategoryDisplayView> {

    Optional<CategoryDisplayView> findByIdAndLanguageCode(Integer id, String languageCode);

}

package org.example.library.recommendations.repository;

import org.example.library.recommendations.domain.GenreMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreMappingRepository extends JpaRepository<GenreMapping, Integer> {

    Optional<GenreMapping> findByCategoryId(Integer categoryId);

    @Query("SELECT g.categoryId FROM GenreMapping g")
    List<Integer> findAllCategoryIds();

    @Query("SELECT MAX(g.vectorIndex) FROM GenreMapping g")
    Optional<Integer> findMaxVectorIndex();
}

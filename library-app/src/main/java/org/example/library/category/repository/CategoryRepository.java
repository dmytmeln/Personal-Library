package org.example.library.category.repository;

import org.example.library.category.domain.Category;
import org.example.library.category.dto.CategoryWithBooksCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("""
            SELECT
                c.id AS id,
                c.name AS name,
                c.description AS description,
                COUNT(b) AS booksCount
            FROM Category c
            LEFT JOIN c.books b
            WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
            GROUP BY c.id, c.name, c.description
            """)
    Page<CategoryWithBooksCount> searchWithBooksCount(@Param("name") String name, Pageable pageable);

}

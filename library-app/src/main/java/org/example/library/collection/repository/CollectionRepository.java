package org.example.library.collection.repository;

import org.example.library.collection.domain.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Integer> {

    List<Collection> findAllByUserId(Integer userId);

    boolean existsByIdAndUserId(Integer id, Integer userId);

}

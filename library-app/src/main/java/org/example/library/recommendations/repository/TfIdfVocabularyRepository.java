package org.example.library.recommendations.repository;

import org.example.library.recommendations.domain.TfIdfVocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TfIdfVocabularyRepository extends JpaRepository<TfIdfVocabulary, String> {

    void deleteByVersionLessThan(int version);

    List<TfIdfVocabulary> findAllByVersion(int version);

}

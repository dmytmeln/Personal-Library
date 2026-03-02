package org.example.library.recommendations.repository;

import org.example.library.recommendations.domain.VocabularyMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabularyMetadataRepository extends JpaRepository<VocabularyMetadata, Integer> {
}

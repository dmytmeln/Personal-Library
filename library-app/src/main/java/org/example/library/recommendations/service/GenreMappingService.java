package org.example.library.recommendations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.category.repository.CategoryRepository;
import org.example.library.recommendations.config.RecommendationProperties;
import org.example.library.recommendations.domain.GenreMapping;
import org.example.library.recommendations.repository.GenreMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreMappingService {

    private final GenreMappingRepository genreMappingRepository;
    private final CategoryRepository categoryRepository;
    private final RecommendationProperties properties;


    @Transactional
    public boolean hasNewCategories() {
        var existingCategoryIds = genreMappingRepository.findAllCategoryIds();
        var newCategoryIds = categoryRepository.findAllIds().stream()
                .filter(id -> !existingCategoryIds.contains(id))
                .toList();
        return !newCategoryIds.isEmpty();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateGenreMapping() {
        var mappingCount = genreMappingRepository.count();
        var categoryCount = categoryRepository.count();

        if (categoryCount <= mappingCount) {
            log.info("No new categories to add to genre mapping.");
            return;
        }

        var existingCategoryIds = genreMappingRepository.findAllCategoryIds();
        var newCategoryIds = categoryRepository.findAllIds().stream()
                .filter(id -> !existingCategoryIds.contains(id))
                .toList();

        if (newCategoryIds.isEmpty()) {
            log.info("No new categories to add to genre mapping.");
            return;
        }

        var currentCount = (int) mappingCount;
        var newCount = newCategoryIds.size();
        var totalCount = currentCount + newCount;

        if (totalCount > properties.getGenreVectorSize()) {
            log.error("Cannot add {} new categories. Total would be {} which exceeds max size of {}. " +
                            "Existing mappings: {}, Available slots: {}",
                    newCount, totalCount, properties.getGenreVectorSize(), currentCount, properties.getGenreVectorSize() - currentCount);
            newCategoryIds = newCategoryIds.subList(0, properties.getGenreVectorSize() - currentCount);
        }

        var nextIndex = genreMappingRepository.findMaxVectorIndex().orElse(-1);

        var newMappings = new ArrayList<GenreMapping>();
        for (var categoryId : newCategoryIds) {
            var mapping = GenreMapping.builder()
                    .categoryId(categoryId)
                    .vectorIndex(++nextIndex)
                    .build();
            newMappings.add(mapping);
        }

        genreMappingRepository.saveAllAndFlush(newMappings);
        log.info("Added {} new category mappings to genre_mapping. Total mappings: {}",
                newMappings.size(), currentCount + newMappings.size());
    }

}

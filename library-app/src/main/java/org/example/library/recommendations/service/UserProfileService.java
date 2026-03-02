package org.example.library.recommendations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.domain.LibraryBookStatus;
import org.example.library.library_book.repository.LibraryBookRepository;
import org.example.library.recommendations.config.RecommendationProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final LibraryBookRepository libraryBookRepository;
    private final RecommendationProperties properties;


    public float[] calculateUserProfileVector(Integer userId) {
        var userLibrary = libraryBookRepository.findAllWithVectorsByUserId(userId);

        if (userLibrary.isEmpty()) {
            log.warn("User {} has no books with vectors. Returning null.", userId);
            return null;
        }

        int vectorSize = properties.getTotalVectorSize();
        var combinedVector = new float[vectorSize];
        float totalWeight = 0.0f;

        for (LibraryBook lb : userLibrary) {
            float weight = determineWeight(lb);
            float recencyFactor = calculateRecencyFactor(lb.getAddedAt());
            float finalWeight = weight * recencyFactor;

            var bookVector = lb.getBook().getDescriptionVector();

            if (bookVector == null || bookVector.length != vectorSize) continue;

            for (int i = 0; i < vectorSize; i++) {
                combinedVector[i] += bookVector[i] * finalWeight;
            }
            totalWeight += Math.abs(finalWeight);
        }

        if (totalWeight > 0) {
            for (int i = 0; i < vectorSize; i++) {
                combinedVector[i] /= totalWeight;
            }
        }

        return combinedVector;
    }

    private float calculateRecencyFactor(LocalDateTime addedAt) {
        if (addedAt == null) return 1.0f;
        long monthsPassed = ChronoUnit.MONTHS.between(addedAt, LocalDateTime.now());
        return (float) Math.exp(-properties.getRecencyDecayFactor() * monthsPassed);
    }

    private float determineWeight(LibraryBook lb) {
        if (LibraryBookStatus.FAVORITE == lb.getStatus()) return properties.getFavoriteWeight();

        Byte rating = lb.getRating();
        if (rating == null) return properties.getNoRatingWeight();

        return switch (rating) {
            case 5 -> properties.getRating5Weight();
            case 4 -> properties.getRating4Weight();
            case 3 -> properties.getRating3Weight();
            default -> properties.getLowRatingWeight();
        };
    }

}

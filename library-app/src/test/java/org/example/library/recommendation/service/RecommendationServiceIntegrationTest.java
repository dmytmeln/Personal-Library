package org.example.library.recommendation.service;

import org.example.library.config.AbstractServiceIntegrationTest;
import org.example.library.recommendation.domain.UserProfileVector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.library_book.domain.LibraryBookStatus.READING;

class RecommendationServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private RecommendationService recommendationService;

    @Test
    void shouldReturnRecommendationsBasedOnVector() {
        var testUser = saveUser();
        float[] userVector = new float[384];
        userVector[0] = 1.0f;
        testDbClient.saveUserProfileVector(UserProfileVector.builder()
                .userId(testUser.getId())
                .embedding(userVector)
                .build());

        float[] book3Vector = new float[384];
        book3Vector[0] = 0.9f;
        saveBook(b -> b.title("Similar Book 2").embedding(book3Vector).publishYear((short)2020));

        float[] book1Vector = new float[384];
        book1Vector[0] = 0.5f;
        saveBook(b -> b.title("Similar Book 1").embedding(book1Vector).publishYear((short)2020));

        float[] book2Vector = new float[384];
        book2Vector[1] = 1.0f;
        saveBook(b -> b.title("Dissimilar Book").embedding(book2Vector).publishYear((short)2020));

        var recommendations = recommendationService.getRecommendations(testUser.getId(), 5);

        assertThat(recommendations).hasSize(3);
        assertThat(recommendations.get(0).getTitle()).isEqualTo("Similar Book 2");
        assertThat(recommendations.get(1).getTitle()).isEqualTo("Similar Book 1");
        assertThat(recommendations.get(2).getTitle()).isEqualTo("Dissimilar Book");
    }

    @Test
    void shouldReturnSimilarBooksExcludingTheTargetBook() {
        var testUser = saveUser();
        float[] targetVector = new float[384];
        targetVector[0] = 1.0f;
        var targetBook = saveBook(b -> b.title("Target Book").embedding(targetVector).publishYear((short)2020));
        float[] similarVector = new float[384];
        similarVector[0] = 0.9f;
        saveBook(b -> b.title("Similar Book").embedding(similarVector).publishYear((short)2020));

        var similarBooks = recommendationService.getSimilarBooks(targetBook.getId(), testUser.getId(), 5);

        assertThat(similarBooks).hasSize(1);
        assertThat(similarBooks.get(0).getTitle()).isEqualTo("Similar Book");
    }

    @Test
    void shouldReturnNewArrivalsForCurrentYear() {
        var testUser = saveUser();
        saveBook(b -> b.title("Old Book").embedding(new float[384]).publishYear((short)1999));
        saveBook(b -> b.title("New Book").embedding(new float[384]).publishYear((short)Year.now().getValue()));

        var newArrivals = recommendationService.getNewArrivals(testUser.getId(), 5);

        assertThat(newArrivals).hasSize(1);
        assertThat(newArrivals.get(0).getTitle()).isEqualTo("New Book");
    }

    @Test
    void shouldReturnPopularBooksRecently() {
        var testUser = saveUser();
        var book1 = saveBook(b -> b.title("Popular Book 1").embedding(new float[384]).publishYear((short)2020));
        var book2 = saveBook(b -> b.title("Popular Book 2").embedding(new float[384]).publishYear((short)2020));
        var otherUser1 = saveUser(u -> u.email("o1@example.com").fullName("O1").password("p"));
        var otherUser2 = saveUser(u -> u.email("o2@example.com").fullName("O2").password("p"));
        saveLibraryBook(lb -> lb.user(otherUser1).book(book1).status(READING));
        saveLibraryBook(lb -> lb.user(otherUser2).book(book1).status(READING));
        saveLibraryBook(lb -> lb.user(otherUser1).book(book2).status(READING));

        var popularBooks = recommendationService.getPopularBooks(testUser.getId(), 5);

        assertThat(popularBooks).hasSize(2);
        assertThat(popularBooks.get(0).getTitle()).as("2 adds").isEqualTo("Popular Book 1");
        assertThat(popularBooks.get(1).getTitle()).as("1 add").isEqualTo("Popular Book 2");
    }

    @Test
    void shouldReturnTrendingInFavoriteGenres() {
        var testUser = saveUser();
        var fictionCategory = saveCategory(c -> c.name("Fiction").description("Fiction category"));
        var otherCategory = saveCategory(c -> c.name("Sci-Fi").description("Sci-Fi"));
        var book1 = saveBook(b -> b.title("Fiction Book 1").embedding(new float[384]).publishYear((short)2020).category(fictionCategory));
        saveBook(b -> b.title("Fiction Book 2").embedding(new float[384]).publishYear((short)2020).category(fictionCategory));
        saveBook(b -> b.title("Sci-Fi Book").embedding(new float[384]).publishYear((short)2020).category(otherCategory));
        saveLibraryBook(lb -> lb.user(testUser).book(book1).status(READING));

        var trending = recommendationService.getTrendingInFavoriteGenres(testUser.getId(), 5);

        assertThat(trending).hasSize(1);
        assertThat(trending.get(0).getTitle()).isEqualTo("Fiction Book 2");
    }

    @Test
    void shouldReturnBooksBasedOnMoodQuery() {
        var testUser = saveUser();
        float[] vector1 = new float[384];
        vector1[0] = 0.9f;
        saveBook(b -> b.title("Space Adventure").embedding(vector1).publishYear((short)2024));

        float[] vector2 = new float[384];
        vector2[0] = 0.8f;
        saveBook(b -> b.title("Galactic Journey").embedding(vector2).publishYear((short)2023));

        float[] vector3 = new float[384];
        vector3[1] = 0.9f;
        saveBook(b -> b.title("Historical Romance").embedding(vector3).publishYear((short)2022));

        float[] vector4 = new float[384];
        vector4[1] = 0.8f;
        saveBook(b -> b.title("Medieval Love").embedding(vector4).publishYear((short)2021));

        float[] vector5 = new float[384];
        vector5[5] = 0.9f;
        saveBook(b -> b.title("Cooking Basics").embedding(vector5).publishYear((short)2020));

        var results = recommendationService.searchByMood("space trip", testUser.getId(), 2);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).containsAnyOf("Space Adventure", "Galactic Journey");
    }

}

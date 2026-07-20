package org.example.library.recommendation.repository;

import org.example.library.config.AbstractRepositoryTest;
import org.example.library.recommendation.domain.UserProfileVector;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.config.EntityRecursiveComparisonConfigs.USER_PROFILE_VECTOR_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.USER_PROFILE_VECTOR_SAVED;
import static org.example.library.user.domain.Role.USER;

class UserProfileVectorRepositoryTest extends AbstractRepositoryTest<UserProfileVectorRepository> {

    @Test
    void save_ShouldPersistUserProfileVector() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        float[] embedding = createEmbedding();
        UserProfileVector expected = UserProfileVector.builder()
                .userId(user.getId())
                .embedding(embedding)
                .updatedAt(LocalDateTime.of(2026, 7, 22, 12, 0))
                .build();

        UserProfileVector actual = repository.save(expected);

        assertThat(actual)
                .usingRecursiveComparison(USER_PROFILE_VECTOR_SAVED)
                .isEqualTo(expected);
        UserProfileVector dbState = testDbClient.findUserProfileVectorById(actual.getUserId());
        assertThat(dbState)
                .isNotNull()
                .usingRecursiveComparison(USER_PROFILE_VECTOR_DIRECT_FIELDS)
                .isEqualTo(actual);
    }

    @Test
    @Transactional
    void findById_ShouldReturnUserProfileVector_WhenExists() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        float[] embedding = createEmbedding();
        UserProfileVector profile = UserProfileVector.builder()
                .userId(user.getId())
                .embedding(embedding)
                .updatedAt(LocalDateTime.of(2026, 7, 22, 12, 0))
                .build();
        testDbClient.saveUserProfileVector(profile);

        Optional<UserProfileVector> actual = repository.findById(user.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(USER_PROFILE_VECTOR_DIRECT_FIELDS)
                .isEqualTo(profile);
    }

    @Test
    void delete_ShouldRemoveUserProfileVector() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        float[] embedding = createEmbedding();
        UserProfileVector profile = UserProfileVector.builder()
                .userId(user.getId())
                .embedding(embedding)
                .updatedAt(LocalDateTime.of(2026, 7, 22, 12, 0))
                .build();
        testDbClient.saveUserProfileVector(profile);

        repository.deleteById(user.getId());

        assertThat(testDbClient.findUserProfileVectorById(user.getId())).isNull();
    }

    private User createUser(String email) {
        return User.builder()
                .email(email)
                .fullName("Test User")
                .password("password")
                .role(USER)
                .build();
    }

    private float[] createEmbedding() {
        float[] embedding = new float[384];
        embedding[0] = 0.5f;
        embedding[1] = -0.5f;
        embedding[2] = 0.9f;
        return embedding;
    }

}

package org.example.library.auth.repository;

import org.example.library.auth.domain.RefreshToken;
import org.example.library.config.AbstractRepositoryTest;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.Test;

import org.hibernate.Hibernate;

import java.time.Instant;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MICROS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.config.EntityRecursiveComparisonConfigs.REFRESH_TOKEN_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.REFRESH_TOKEN_SAVED;
import static org.example.library.config.EntityRecursiveComparisonConfigs.USER_DIRECT_FIELDS;
import static org.example.library.user.domain.Role.USER;

class RefreshTokenRepositoryTest extends AbstractRepositoryTest<RefreshTokenRepository> {

    @Test
    void save_ShouldPersistRefreshToken_AndNotCascadeUser() {
        User user = createUser();
        testDbClient.saveUser(user);
        long initialUserCount = testDbClient.countUsers();
        RefreshToken expected = createRefreshToken(user);

        RefreshToken actual = repository.save(expected);

        assertThat(actual)
                .usingRecursiveComparison(REFRESH_TOKEN_SAVED)
                .isEqualTo(expected);
        assertThat(testDbClient.findRefreshTokenById(actual.getId()))
                .isNotNull()
                .usingRecursiveComparison(REFRESH_TOKEN_DIRECT_FIELDS)
                .isEqualTo(actual);
        assertThat(testDbClient.countUsers()).isEqualTo(initialUserCount);
    }

    @Test
    void findById_ShouldReturnRefreshToken_WhenTokenExists() {
        User user = createUser();
        testDbClient.saveUser(user);
        RefreshToken token = createRefreshToken(user);
        testDbClient.saveRefreshToken(token);

        transactionTemplate.executeWithoutResult(status -> {
            Optional<RefreshToken> actual = repository.findById(token.getId());

            assertThat(actual).isPresent();
            assertThat(actual.get())
                    .usingRecursiveComparison(REFRESH_TOKEN_DIRECT_FIELDS)
                    .isEqualTo(token);
            assertThat(actual.get().getUser())
                    .extracting(Hibernate::unproxy)
                    .usingRecursiveComparison(USER_DIRECT_FIELDS)
                    .isEqualTo(user);
        });
    }

    @Test
    void purgeExpiredTokens_ShouldRemoveExpiredTokens() {
        User user = createUser();
        testDbClient.saveUser(user);
        RefreshToken expired = createRefreshToken(user);
        expired.setExpiryDate(Instant.now().minusSeconds(60).truncatedTo(MICROS));
        testDbClient.saveRefreshToken(expired);
        RefreshToken valid = createRefreshToken(user);
        valid.setExpiryDate(Instant.now().plusSeconds(3600).truncatedTo(MICROS));
        testDbClient.saveRefreshToken(valid);

        repository.purgeExpiredTokens(Instant.now());

        assertThat(testDbClient.findRefreshTokenById(expired.getId())).isNull();
        assertThat(testDbClient.findRefreshTokenById(valid.getId())).isNotNull();
    }

    @Test
    void revokeAllByUserId_ShouldRevokeAllTokensForUser() {
        User userA = createUser();
        testDbClient.saveUser(userA);
        User userB = createUser();
        userB.setEmail("userb@example.com");
        testDbClient.saveUser(userB);
        RefreshToken tokenA1 = createRefreshToken(userA);
        testDbClient.saveRefreshToken(tokenA1);
        RefreshToken tokenA2 = createRefreshToken(userA);
        testDbClient.saveRefreshToken(tokenA2);
        RefreshToken tokenB = createRefreshToken(userB);
        testDbClient.saveRefreshToken(tokenB);

        transactionTemplate.executeWithoutResult(status -> repository.revokeAllByUserId(userA.getId()));

        assertThat(testDbClient.findRefreshTokenById(tokenA1.getId()).isRevoked()).isTrue();
        assertThat(testDbClient.findRefreshTokenById(tokenA2.getId()).isRevoked()).isTrue();
        assertThat(testDbClient.findRefreshTokenById(tokenB.getId()).isRevoked()).isFalse();
    }

    @Test
    void delete_ShouldRemoveToken_ButKeepUser() {
        User user = createUser();
        testDbClient.saveUser(user);
        RefreshToken token = createRefreshToken(user);
        testDbClient.saveRefreshToken(token);
        long initialUserCount = testDbClient.countUsers();

        repository.delete(token);

        assertThat(testDbClient.findRefreshTokenById(token.getId())).isNull();
        assertThat(testDbClient.countUsers()).isEqualTo(initialUserCount);
    }

    private User createUser() {
        return User.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password123")
                .role(USER)
                .build();
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setRevoked(false);
        token.setRefreshTokenHash("hash123");
        token.setExpiryDate(Instant.now().plusSeconds(3600).truncatedTo(MICROS));
        return token;
    }

}

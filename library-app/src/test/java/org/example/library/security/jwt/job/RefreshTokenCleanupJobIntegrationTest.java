package org.example.library.security.jwt.job;

import org.example.library.auth.domain.RefreshToken;
import org.example.library.auth.repository.RefreshTokenRepository;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class RefreshTokenCleanupJobIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenCleanupJob job;

    @BeforeEach
    void cleanDbBefore() {
        testDbClient.cleanDatabase();
    }

    @AfterEach
    void tearDownEach() {
        testDbClient.cleanDatabase();
    }

    @Test
    void shouldDeleteExpiredTokens() {
        var user = saveUser();
        saveToken(user, Instant.now().minus(1, ChronoUnit.HOURS));
        saveToken(user, Instant.now().minus(1, ChronoUnit.MINUTES));
        var validToken = saveToken(user, Instant.now().plus(1, ChronoUnit.HOURS));

        job.cleanupExpiredTokens();

        var remainingTokens = refreshTokenRepository.findAll();
        assertThat(remainingTokens).hasSize(1);
        assertThat(remainingTokens.get(0).getId()).isEqualTo(validToken.getId());
    }

    private User saveUser() {
        var user = User.builder()
                .email("user@test.com")
                .fullName("Test User")
                .password("password")
                .role(USER)
                .build();

        testDbClient.saveUser(user);
        return user;
    }

    private RefreshToken saveToken(User user, Instant expiryDate) {
        var token = new RefreshToken();
        token.setUser(user);
        token.setExpiryDate(expiryDate);
        token.setRefreshTokenHash("some-hash");
        token.setCreatedAt(Instant.now());

        testDbClient.saveRefreshToken(token);
        return token;
    }

}


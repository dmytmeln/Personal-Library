package org.example.library.security.jwt.job;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.library.auth.domain.RefreshToken;
import org.example.library.auth.repository.RefreshTokenRepository;
import org.example.library.config.PostgresTestContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.example.library.user.domain.User;
import org.example.library.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RefreshTokenCleanupJobIntegrationTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenCleanupJob job;

    @DynamicPropertySource
    static void setPostgresProperties(DynamicPropertyRegistry registry) {
        PostgresTestContainer.setProperties(registry);
    }

    @Test
    void shouldDeleteExpiredTokens() {
        var user = saveUser();
        saveToken(user, Instant.now().minus(1, ChronoUnit.HOURS));
        saveToken(user, Instant.now().minus(1, ChronoUnit.MINUTES));
        var validToken = saveToken(user, Instant.now().plus(1, ChronoUnit.HOURS));
        em.flush();
        em.clear();

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
                .build();
        return userRepository.save(user);
    }

    private RefreshToken saveToken(User user, Instant expiryDate) {
        var token = new RefreshToken();
        token.setUser(user);
        token.setExpiryDate(expiryDate);
        token.setRefreshTokenHash("some-hash");
        return refreshTokenRepository.save(token);
    }

}

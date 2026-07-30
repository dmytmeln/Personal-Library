package org.example.library.security.jwt.job;

import org.example.library.auth.repository.RefreshTokenRepository;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenCleanupJobIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenCleanupJob job;

    @Test
    void shouldDeleteExpiredTokens() {
        var user = saveUser();
        saveRefreshToken(r -> r.user(user).expiryDate(Instant.now().minus(1, ChronoUnit.HOURS)));
        saveRefreshToken(r -> r.user(user).expiryDate(Instant.now().minus(1, ChronoUnit.MINUTES)));
        var validToken = saveRefreshToken(r -> r.user(user).expiryDate(Instant.now().plus(1, ChronoUnit.HOURS)));

        job.cleanupExpiredTokens();

        var remainingTokens = refreshTokenRepository.findAll();
        assertThat(remainingTokens).hasSize(1);
        assertThat(remainingTokens.get(0).getId()).isEqualTo(validToken.getId());
    }

}

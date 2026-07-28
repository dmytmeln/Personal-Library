package org.example.library.security.jwt;

import org.example.library.auth.domain.RefreshToken;
import org.example.library.auth.repository.RefreshTokenRepository;
import org.example.library.auth.service.RefreshTokenService;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.example.library.user.domain.User;
import org.example.library.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class RefreshTokenServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService service;

    @BeforeEach
    void cleanDbBefore() {
        testDbClient.cleanDatabase();
    }

    @AfterEach
    void tearDownEach() {
        testDbClient.cleanDatabase();
    }

    @Test
    void shouldGenerateNewTokens() {
        var user = saveUser();

        var response = service.generateNewTokens(user);

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.refreshTokenId()).isNotNull();
        var savedToken = testDbClient.findRefreshTokenById(response.refreshTokenId());
        assertThat(savedToken).isNotNull();
        assertThat(savedToken.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedToken.isRevoked()).isFalse();
        assertThat(savedToken.getExpiryDate()).isAfter(Instant.now());
    }

    @Test
    void shouldRefreshToken() {
        var user = saveUser();
        var initialResponse = service.generateNewTokens(user);

        var response = service.refreshToken(initialResponse.refreshToken(), initialResponse.refreshTokenId());

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.refreshTokenId()).isNotNull();
        assertThat(response.refreshTokenId()).isNotEqualTo(initialResponse.refreshTokenId());
        var oldToken = testDbClient.findRefreshTokenById(initialResponse.refreshTokenId());
        assertThat(oldToken).isNotNull();
        assertThat(oldToken.isRevoked())
                .as("Old token should be revoked")
                .isTrue();
        var newToken = testDbClient.findRefreshTokenById(response.refreshTokenId());
        assertThat(newToken).isNotNull();
        assertThat(newToken.isRevoked()).isFalse();
    }

    @Test
    void shouldThrowBadCredentialsWhenTokenIdNotFound() {
        var user = saveUser();
        var initialResponse = service.generateNewTokens(user);

        assertThatThrownBy(() -> service.refreshToken(initialResponse.refreshToken(), -1))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Unknown token");
    }

    @Test
    void shouldThrowBadCredentialsWhenTokenHashDoesNotMatch() {
        var user = saveUser();
        var initialResponse = service.generateNewTokens(user);

        assertThatThrownBy(() -> service.refreshToken("invalid-token", initialResponse.refreshTokenId()))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void shouldThrowBadCredentialsWhenTokenIsExpired() {
        var user = saveUser();
        var initialResponse = service.generateNewTokens(user);
        var token = refreshTokenRepository.findById(initialResponse.refreshTokenId())
                .orElseThrow(() -> new AssertionError("Refresh token not found"));
        token.setExpiryDate(Instant.now().minus(1, ChronoUnit.HOURS));
        refreshTokenRepository.save(token);

        assertThatThrownBy(() -> service.refreshToken(initialResponse.refreshToken(), initialResponse.refreshTokenId()))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void shouldThrowSecurityExceptionAndRevokeWhenEmailMismatch() {
        var user = saveUser();
        var initialResponse = service.generateNewTokens(user);
        user.setEmail("new-email@test.com");
        userRepository.save(user);

        assertThatThrownBy(() -> service.refreshToken(initialResponse.refreshToken(), initialResponse.refreshTokenId()))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Identity mismatch. Please log in again.");

        var token = testDbClient.findRefreshTokenById(initialResponse.refreshTokenId());
        assertThat(token).isNotNull();
        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    void shouldThrowSecurityExceptionAndRevokeAllWhenTokenReused() {
        var user = saveUser();
        var initialResponse = service.generateNewTokens(user);
        service.generateNewTokens(user);

        var token = refreshTokenRepository.findById(initialResponse.refreshTokenId())
                .orElseThrow(() -> new AssertionError("Refresh token not found"));
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        assertThatThrownBy(() -> service.refreshToken(initialResponse.refreshToken(), initialResponse.refreshTokenId()))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Breach detected: Refresh token reused.");

        var tokens = refreshTokenRepository.findAll();
        assertThat(tokens).as("All tokens for the user should be revoked").allMatch(RefreshToken::isRevoked);
    }

    @Test
    void shouldIssueTokensOnEmailUpdate() {
        var user = saveUser();
        service.generateNewTokens(user);
        service.generateNewTokens(user);

        var response = service.issueTokensOnEmailUpdate(user);

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        var allTokens = refreshTokenRepository.findAll();
        assertThat(allTokens).filteredOn(RefreshToken::isRevoked).hasSize(2);
        assertThat(allTokens).filteredOn(t -> !t.isRevoked())
                .hasSize(1)
                .first()
                .extracting(RefreshToken::getId)
                .isEqualTo(response.refreshTokenId());
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

}


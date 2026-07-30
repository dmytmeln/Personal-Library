package org.example.library.config;

import org.example.library.auth.domain.RefreshToken;
import org.example.library.user.domain.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class RefreshTokenConfigurer {

    private final TestDbClient testDbClient;

    private User user;
    private boolean userSet;
    private String refreshTokenHash = "test-hash";
    private Instant expiryDate = Instant.now().plus(365, ChronoUnit.DAYS);
    private boolean revoked = false;

    public RefreshTokenConfigurer(TestDbClient testDbClient) {
        this.testDbClient = testDbClient;
    }

    public RefreshTokenConfigurer user(User user) {
        this.user = user;
        this.userSet = true;
        return this;
    }

    public RefreshTokenConfigurer refreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
        return this;
    }

    public RefreshTokenConfigurer expiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public RefreshTokenConfigurer revoked(boolean revoked) {
        this.revoked = revoked;
        return this;
    }

    public RefreshToken save() {
        if (!userSet) {
            user = new UserConfigurer(testDbClient).save();
        }

        var token = new RefreshToken();
        token.setUser(user);
        token.setRefreshTokenHash(refreshTokenHash);
        token.setExpiryDate(expiryDate);
        token.setRevoked(revoked);

        testDbClient.saveRefreshToken(token);
        return token;
    }

}

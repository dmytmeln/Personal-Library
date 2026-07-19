package org.example.library.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.example.library.user.domain.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "refresh_tokens_seq")
    @SequenceGenerator(name = "refresh_tokens_seq", sequenceName = "refresh_tokens_seq", allocationSize = 20)
    @Column(name = "refresh_token_id")
    private Integer id;

    @ManyToOne(optional = false, fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "refresh_token_hash", nullable = false)
    private String refreshTokenHash;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RefreshToken that)) {
            return false;
        }

        return Objects.equals(this.id, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

}

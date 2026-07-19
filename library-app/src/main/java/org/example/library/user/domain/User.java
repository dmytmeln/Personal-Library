package org.example.library.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.library.auth.domain.RefreshToken;
import org.example.library.auth.domain.RefreshToken_;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static org.example.library.user.domain.Role.USER;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "users_seq")
    @SequenceGenerator(name = "users_seq", sequenceName = "users_seq", allocationSize = 20)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "password")
    private String password;

    @Enumerated(STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = USER;

    @OneToMany(mappedBy = RefreshToken_.USER)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) {
            return false;
        }

        return Objects.equals(this.id, user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

}

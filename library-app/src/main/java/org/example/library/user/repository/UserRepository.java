package org.example.library.user.repository;

import org.example.library.user.domain.Provider;
import org.example.library.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findUserByEmail(String email);

    boolean existsByEmail(String email);

}

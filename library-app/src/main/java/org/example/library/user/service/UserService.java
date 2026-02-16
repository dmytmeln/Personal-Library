package org.example.library.user.service;

import lombok.RequiredArgsConstructor;
import org.example.library.auth.dto.UserRegisterRequest;
import org.example.library.exception.BadRequestException;
import org.example.library.user.domain.Provider;
import org.example.library.user.domain.User;
import org.example.library.user.dto.UserResponse;
import org.example.library.user.mapper.UserMapper;
import org.example.library.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    public UserResponse register(UserRegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        var user = mapper.toHostUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var savedUser = repository.save(user);
        return mapper.toResponse(savedUser);
    }

    public User syncUser(String email, String fullName, String providerId, Provider provider) {
        return repository.findUserByEmail(email)
                .orElseGet(() -> repository.save(
                        mapper.createProvidedUser(email, fullName, providerId, provider)
                ));
    }

}

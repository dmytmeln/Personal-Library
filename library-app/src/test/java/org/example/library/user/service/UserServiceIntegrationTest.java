package org.example.library.user.service;

import org.example.library.auth.dto.UserRegisterRequest;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.example.library.user.domain.User;
import org.example.library.user.dto.UpdateProfileRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class UserServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @BeforeEach
    void cleanDbBefore() {
        testDbClient.cleanDatabase();
    }

    @AfterEach
    void tearDownEach() {
        testDbClient.cleanDatabase();
    }

    @Test
    void shouldRegisterUser() {
        var request = UserRegisterRequest.builder()
                .email("newuser@example.com")
                .fullName("New User")
                .password("password123")
                .build();

        var response = userService.register(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
        assertThat(response.getFullName()).isEqualTo("New User");
        assertThat(response.getRole()).isEqualTo(USER);
        var savedUser = testDbClient.findUserById(response.getId());
        assertThat(savedUser).isNotNull();
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    void shouldThrowBadRequestWhenRegisteringWithExistingEmail() {
        var existingUser = User.builder()
                .email("existing@example.com")
                .fullName("Existing User")
                .password("pass")
                .role(USER)
                .build();
        testDbClient.saveUser(existingUser);

        var request = UserRegisterRequest.builder()
                .email("existing@example.com")
                .fullName("New User")
                .password("password123")
                .build();

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.auth.email_already_registered");
    }

    @Test
    void shouldUpdateProfile() {
        var user = User.builder()
                .email("user@example.com")
                .fullName("Old Name")
                .password("pass")
                .role(USER)
                .build();
        testDbClient.saveUser(user);

        var request = UpdateProfileRequest.builder()
                .email("user@example.com")
                .fullName("New Name")
                .build();

        var response = userService.updateProfile(user.getId(), request);

        assertThat(response.userResponse().getFullName()).isEqualTo("New Name");
        assertThat(response.tokenResponse()).isNull();
        var updatedUser = testDbClient.findUserById(user.getId());
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getFullName()).isEqualTo("New Name");
    }

    @Test
    void shouldUpdateProfileAndIssueNewTokensWhenEmailChanges() {
        var user = User.builder()
                .email("old@example.com")
                .fullName("User")
                .password("pass")
                .role(USER)
                .build();
        testDbClient.saveUser(user);

        var request = UpdateProfileRequest.builder()
                .email("new@example.com")
                .fullName("User")
                .build();

        var response = userService.updateProfile(user.getId(), request);

        assertThat(response.userResponse().getEmail()).isEqualTo("new@example.com");
        assertThat(response.tokenResponse()).isNotNull();
        assertThat(response.tokenResponse().accessToken()).isNotBlank();
        var updatedUser = testDbClient.findUserById(user.getId());
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentUser() {
        var request = UpdateProfileRequest.builder()
                .email("test@example.com")
                .fullName("Test")
                .build();

        assertThatThrownBy(() -> userService.updateProfile(-1, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.user.not_found");
    }

    @Test
    void shouldThrowBadRequestWhenUpdatingToExistingEmail() {
        var user1 = User.builder()
                .email("user1@example.com")
                .fullName("User One")
                .password("pass")
                .role(USER)
                .build();
        var user2 = User.builder()
                .email("user2@example.com")
                .fullName("User Two")
                .password("pass")
                .role(USER)
                .build();
        testDbClient.saveUser(user1);
        testDbClient.saveUser(user2);

        var request = UpdateProfileRequest.builder()
                .email("user2@example.com")
                .fullName("User One Updated")
                .build();

        assertThatThrownBy(() -> userService.updateProfile(user1.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.auth.email_already_registered");
    }

}


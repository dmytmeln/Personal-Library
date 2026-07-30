package org.example.library.user.service;

import org.example.library.auth.dto.UserRegisterRequest;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.example.library.user.domain.Role;
import org.example.library.user.dto.UpdateProfileRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

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
        assertThat(response.getRole()).isEqualTo(Role.USER);
        var savedUser = testDbClient.findUserById(response.getId());
        assertThat(savedUser).isNotNull();
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    void shouldThrowBadRequestWhenRegisteringWithExistingEmail() {
        var existingUser = saveUser(u -> u.email("existing@example.com").fullName("Existing User").password("pass"));

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
        var user = saveUser(u -> u.email("user@example.com").fullName("Old Name").password("pass"));

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
        var user = saveUser(u -> u.email("old@example.com").fullName("User").password("pass"));

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
        var user1 = saveUser(u -> u.email("user1@example.com").fullName("User One").password("pass"));
        var user2 = saveUser(u -> u.email("user2@example.com").fullName("User Two").password("pass"));

        var request = UpdateProfileRequest.builder()
                .email("user2@example.com")
                .fullName("User One Updated")
                .build();

        assertThatThrownBy(() -> userService.updateProfile(user1.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.auth.email_already_registered");
    }

}

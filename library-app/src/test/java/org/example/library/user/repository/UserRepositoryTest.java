package org.example.library.user.repository;

import org.example.library.config.AbstractRepositoryTest;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.config.EntityRecursiveComparisonConfigs.USER_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.USER_SAVED;
import static org.example.library.user.domain.Role.USER;

class UserRepositoryTest extends AbstractRepositoryTest<UserRepository> {

    @Test
    void save_ShouldPersistUser() {
        User expectedUser = createUser();

        User actual = repository.save(expectedUser);

        assertThat(actual)
                .usingRecursiveComparison(USER_SAVED)
                .isEqualTo(expectedUser);
        assertThat(testDbClient.findUserById(actual.getId()))
                .isNotNull()
                .usingRecursiveComparison(USER_DIRECT_FIELDS)
                .isEqualTo(actual);
    }

    @Test
    void update_ShouldModifyExistingUser() {
        User expected = createUser();
        testDbClient.saveUser(expected);
        expected.setFullName("Updated Name");

        User actual = repository.save(expected);

        assertThat(actual)
                .usingRecursiveComparison(USER_DIRECT_FIELDS)
                .isEqualTo(expected);
        assertThat(testDbClient.findUserById(expected.getId()))
                .isNotNull()
                .usingRecursiveComparison(USER_DIRECT_FIELDS)
                .isEqualTo(actual);
        assertThat(actual.getId()).isEqualTo(expected.getId());
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        User user = createUser();
        testDbClient.saveUser(user);

        Optional<User> actual = repository.findById(user.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(USER_DIRECT_FIELDS)
                .isEqualTo(user);
    }

    @Test
    void findUserByEmail_ShouldReturnUser_WhenUserExists() {
        User user = createUser();
        testDbClient.saveUser(user);

        Optional<User> actual = repository.findUserByEmail("test@example.com");

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(USER_DIRECT_FIELDS)
                .isEqualTo(user);
    }

    @Test
    void findUserByEmail_ShouldReturnEmpty_WhenUserDoesNotExist() {
        Optional<User> actual = repository.findUserByEmail("nonexistent@example.com");

        assertThat(actual).isEmpty();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenUserExists() {
        User user = createUser();
        testDbClient.saveUser(user);

        boolean actual = repository.existsByEmail("test@example.com");

        assertThat(actual).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenUserDoesNotExist() {
        boolean actual = repository.existsByEmail("nonexistent@example.com");

        assertThat(actual).isFalse();
    }

    @Test
    void delete_ShouldRemoveUser() {
        User user = createUser();
        testDbClient.saveUser(user);

        repository.delete(user);

        assertThat(testDbClient.findUserById(user.getId())).isNull();
    }

    private User createUser() {
        return User.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password123")
                .role(USER)
                .build();
    }

}

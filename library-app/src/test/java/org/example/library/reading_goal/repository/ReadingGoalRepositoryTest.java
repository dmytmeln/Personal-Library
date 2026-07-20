package org.example.library.reading_goal.repository;

import org.example.library.config.AbstractRepositoryTest;
import org.example.library.reading_goal.domain.ReadingGoal;
import org.example.library.user.domain.User;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.config.EntityRecursiveComparisonConfigs.READING_GOAL_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.READING_GOAL_SAVED;
import static org.example.library.config.EntityRecursiveComparisonConfigs.USER_DIRECT_FIELDS;
import static org.example.library.user.domain.Role.USER;

class ReadingGoalRepositoryTest extends AbstractRepositoryTest<ReadingGoalRepository> {

    @Test
    void save_ShouldPersistReadingGoal_AndNotCascadeUser() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        long initialUserCount = testDbClient.countUsers();
        ReadingGoal expected = createReadingGoal(user, 2026, 12, 1000);

        ReadingGoal actual = repository.save(expected);

        assertThat(actual)
                .usingRecursiveComparison(READING_GOAL_SAVED)
                .isEqualTo(expected);
        ReadingGoal dbState = testDbClient.findReadingGoalById(actual.getId());
        assertThat(dbState)
                .isNotNull()
                .usingRecursiveComparison(READING_GOAL_DIRECT_FIELDS)
                .isEqualTo(actual);
        assertThat(testDbClient.countUsers()).isEqualTo(initialUserCount);
    }

    @Test
    @Transactional
    void findById_ShouldReturnReadingGoal_WhenExists() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        ReadingGoal goal = createReadingGoal(user, 2026, 12, 1000);
        testDbClient.saveReadingGoal(goal);

        Optional<ReadingGoal> actual = repository.findById(goal.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(READING_GOAL_DIRECT_FIELDS)
                .isEqualTo(goal);
        assertThat(actual.get().getUser())
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(USER_DIRECT_FIELDS)
                .isEqualTo(user);
    }

    @Test
    void delete_ShouldRemoveReadingGoal_ButKeepUser() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        ReadingGoal goal = createReadingGoal(user, 2026, 12, 1000);
        testDbClient.saveReadingGoal(goal);
        long initialUserCount = testDbClient.countUsers();

        repository.deleteById(goal.getId());

        assertThat(testDbClient.findReadingGoalById(goal.getId())).isNull();
        assertThat(testDbClient.countUsers()).isEqualTo(initialUserCount);
    }

    @Test
    void findByUserIdAndYear_ShouldReturnReadingGoal_WhenExists() {
        User user = createUser("user@example.com");
        testDbClient.saveUser(user);
        ReadingGoal goal = createReadingGoal(user, 2026, 12, 1000);
        testDbClient.saveReadingGoal(goal);

        Optional<ReadingGoal> actual = repository.findByUserIdAndYear(user.getId(), 2026);

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(READING_GOAL_DIRECT_FIELDS)
                .isEqualTo(goal);
    }

    @Test
    void findByUserIdAndYear_ShouldReturnEmpty_WhenDoesNotExist() {
        Optional<ReadingGoal> actual = repository.findByUserIdAndYear(999, 2026);

        assertThat(actual).isEmpty();
    }

    private User createUser(String email) {
        return User.builder()
                .email(email)
                .fullName("Test User")
                .password("password")
                .role(USER)
                .build();
    }

    private ReadingGoal createReadingGoal(User user, Integer year, Integer targetBooks, Integer targetPages) {
        return ReadingGoal.builder()
                .user(user)
                .year(year)
                .targetBooks(targetBooks)
                .targetPages(targetPages)
                .build();
    }

}

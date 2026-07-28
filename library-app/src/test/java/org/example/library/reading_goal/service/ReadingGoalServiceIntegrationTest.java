package org.example.library.reading_goal.service;

import org.example.library.common.exception.NotFoundException;
import org.example.library.config.PostgresTestContainer;
import org.example.library.config.TestDbClient;
import org.example.library.reading_goal.domain.ReadingGoal;
import org.example.library.reading_goal.dto.ReadingGoalDto;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.library.user.domain.Role.USER;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class ReadingGoalServiceIntegrationTest {

    @Autowired
    private TestDbClient testDbClient;

    @Autowired
    private ReadingGoalService service;

    @BeforeEach
    void cleanDbBefore() {
        testDbClient.cleanDatabase();
    }

    @AfterEach
    void tearDownEach() {
        testDbClient.cleanDatabase();
    }

    @Test
    void shouldGetGoal() {
        var user = saveUser();
        var goal = ReadingGoal.builder()
                .user(user)
                .year(2024)
                .targetBooks(10)
                .targetPages(2000)
                .build();
        testDbClient.saveReadingGoal(goal);

        var result = service.getGoal(user.getId(), 2024);

        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getTargetBooks()).isEqualTo(10);
        assertThat(result.getTargetPages()).isEqualTo(2000);
    }

    @Test
    void shouldThrowNotFoundWhenGoalDoesNotExist() {
        var user = saveUser();

        assertThatThrownBy(() -> service.getGoal(user.getId(), 2024))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.reading_goal.not_found");
    }

    @Test
    void shouldCreateNewGoal() {
        var user = saveUser();
        var dto = ReadingGoalDto.builder()
                .year(2024)
                .targetBooks(15)
                .targetPages(3000)
                .build();

        var result = service.createOrUpdate(dto, user.getId());

        assertThat(result.getId()).isNotNull();
        assertThat(result.getYear()).isEqualTo(2024);
        assertThat(result.getTargetBooks()).isEqualTo(15);
        var savedGoal = testDbClient.findReadingGoalById(result.getId());
        assertThat(savedGoal).isNotNull();
        assertThat(savedGoal.getTargetBooks()).isEqualTo(15);
        assertThat(savedGoal.getTargetPages()).isEqualTo(3000);
    }

    @Test
    void shouldUpdateExistingGoal() {
        var user = saveUser();
        var goal = ReadingGoal.builder()
                .user(user)
                .year(2024)
                .targetBooks(10)
                .targetPages(2000)
                .build();
        testDbClient.saveReadingGoal(goal);
        var dto = ReadingGoalDto.builder()
                .year(2024)
                .targetBooks(20)
                .targetPages(4000)
                .build();

        var result = service.createOrUpdate(dto, user.getId());

        assertThat(result.getId()).isEqualTo(goal.getId());
        assertThat(result.getTargetBooks()).isEqualTo(20);
        var updatedGoal = testDbClient.findReadingGoalById(goal.getId());
        assertThat(updatedGoal).isNotNull();
        assertThat(updatedGoal.getTargetBooks()).isEqualTo(20);
        assertThat(updatedGoal.getTargetPages()).isEqualTo(4000);
    }

    @Test
    void shouldDeleteGoal() {
        var user = saveUser();
        var goal = ReadingGoal.builder()
                .user(user)
                .year(2024)
                .targetBooks(10)
                .build();
        testDbClient.saveReadingGoal(goal);

        service.delete(user.getId(), 2024);

        assertThat(testDbClient.findReadingGoalById(goal.getId())).isNull();
    }

    private User saveUser() {
        var user = User.builder()
                .email("user@example.com")
                .fullName("User")
                .password("pass")
                .role(USER)
                .build();

        testDbClient.saveUser(user);
        return user;
    }

}

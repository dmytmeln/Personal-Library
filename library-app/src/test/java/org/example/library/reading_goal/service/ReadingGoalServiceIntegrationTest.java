package org.example.library.reading_goal.service;

import org.example.library.common.exception.NotFoundException;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.example.library.reading_goal.dto.ReadingGoalDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReadingGoalServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ReadingGoalService service;

    @Test
    void shouldGetGoal() {
        var user = saveUser();
        var goal = saveReadingGoal(r -> r.user(user).year(2024).targetBooks(10).targetPages(2000));

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
        var goal = saveReadingGoal(r -> r.user(user).year(2024).targetBooks(10).targetPages(2000));
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
        var goal = saveReadingGoal(r -> r.user(user).year(2024).targetBooks(10));

        service.delete(user.getId(), 2024);

        assertThat(testDbClient.findReadingGoalById(goal.getId())).isNull();
    }

}

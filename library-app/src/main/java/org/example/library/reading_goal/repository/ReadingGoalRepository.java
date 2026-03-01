package org.example.library.reading_goal.repository;

import org.example.library.reading_goal.domain.ReadingGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReadingGoalRepository extends JpaRepository<ReadingGoal, Integer> {

    Optional<ReadingGoal> findByUserIdAndYear(Integer userId, Integer year);

}

package org.example.library.reading_goal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.exception.NotFoundException;
import org.example.library.reading_goal.domain.ReadingGoal;
import org.example.library.reading_goal.dto.ReadingGoalDto;
import org.example.library.reading_goal.mapper.ReadingGoalMapper;
import org.example.library.reading_goal.repository.ReadingGoalRepository;
import org.example.library.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadingGoalService {

    private final ReadingGoalRepository repository;
    private final ReadingGoalMapper mapper;


    @Transactional(readOnly = true)
    public ReadingGoalDto getGoal(Integer userId, Integer year) {
        return repository.findByUserIdAndYear(userId, year)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("error.reading_goal.not_found"));
    }

    @Transactional
    public ReadingGoalDto createOrUpdate(ReadingGoalDto dto, User user) {
        return repository.findByUserIdAndYear(user.getId(), dto.getYear())
                .map(existingGoal -> updateExisting(existingGoal, dto))
                .orElseGet(() -> createNew(dto, user));
    }

    @Transactional
    public void delete(Integer userId, Integer year) {
        repository.findByUserIdAndYear(userId, year)
                .ifPresent(goal -> {
                    repository.delete(goal);
                    log.info("[READING_GOAL_DELETE] User ID: {}, Year: {}", userId, year);
                });
    }

    private ReadingGoalDto createNew(ReadingGoalDto dto, User user) {
        var goal = ReadingGoal.builder()
                .user(user)
                .year(dto.getYear())
                .targetBooks(dto.getTargetBooks())
                .targetPages(dto.getTargetPages())
                .build();
        var savedGoal = repository.save(goal);
        log.info("[READING_GOAL_CREATE] User ID: {}, Year: {}", user.getId(), dto.getYear());
        return mapper.toDto(savedGoal);
    }

    private ReadingGoalDto updateExisting(ReadingGoal existingGoal, ReadingGoalDto dto) {
        mapper.update(existingGoal, dto);
        var savedGoal = repository.save(existingGoal);
        log.info("[READING_GOAL_UPDATE] User ID: {}, Year: {}", existingGoal.getUser().getId(), existingGoal.getYear());
        return mapper.toDto(savedGoal);
    }

}

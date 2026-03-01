package org.example.library.reading_goal.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.reading_goal.dto.ReadingGoalDto;
import org.example.library.reading_goal.service.ReadingGoalService;
import org.example.library.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reading-goals")
@RequiredArgsConstructor
public class ReadingGoalController {

    private final ReadingGoalService service;


    @GetMapping
    public ReadingGoalDto getGoal(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Integer year
    ) {
        return service.getGoal(userDetails.getId(), year);
    }

    @PutMapping
    public ReadingGoalDto saveOrUpdate(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ReadingGoalDto dto
    ) {
        return service.createOrUpdate(dto, userDetails.user());
    }

    @DeleteMapping
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Integer year
    ) {
        service.delete(userDetails.getId(), year);
    }

}

package org.example.library.userBooks.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.user.domain.User;
import org.example.library.userBooks.domain.UserBookStatus;
import org.example.library.userBooks.dto.UserBookDto;
import org.example.library.userBooks.service.UserBookService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/library/books")
@RequiredArgsConstructor
public class UserBookController {

    private final UserBookService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserBookDto> getAll(@AuthenticationPrincipal User user) {
        return service.getAllByUserId(user.getId());
    }

    @GetMapping("/{bookId}")
    public UserBookDto getById(@AuthenticationPrincipal User user, @PathVariable Integer bookId) {
        return service.getById(user.getId(), bookId);
    }

    @PostMapping("/{bookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public UserBookDto create(@AuthenticationPrincipal User user, @PathVariable Integer bookId) {
        return service.create(user.getId(), bookId);
    }

    @PutMapping("/{bookId}/rating")
    @ResponseStatus(HttpStatus.OK)
    public UserBookDto rateBook(@AuthenticationPrincipal User user,
                                @PathVariable Integer bookId,
                                @RequestParam Integer rating
    ) {
        return service.rateBook(user.getId(), bookId, rating);
    }

    @PutMapping("/{bookId}/status")
    @ResponseStatus(HttpStatus.OK)
    public UserBookDto updateStatus(@AuthenticationPrincipal User user,
                                    @PathVariable Integer bookId,
                                    @RequestParam UserBookStatus status
    ) {
        return service.updateStatus(user.getId(), bookId, status);
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Integer bookId) {
        service.delete(user.getId(), bookId);
    }

}

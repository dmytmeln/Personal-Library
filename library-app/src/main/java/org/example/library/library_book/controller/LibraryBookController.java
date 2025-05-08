package org.example.library.library_book.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.library_book.domain.LibraryBookStatus;
import org.example.library.library_book.dto.LibraryBookDto;
import org.example.library.library_book.service.LibraryBookService;
import org.example.library.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/library-books")
@RequiredArgsConstructor
public class LibraryBookController {

    private final LibraryBookService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<LibraryBookDto> getAll(@AuthenticationPrincipal User user,
                                       @RequestParam(defaultValue = "0") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size
    ) {
        return service.getAllByUserId(user.getId(), PageRequest.of(page, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryBookDto create(@AuthenticationPrincipal User user, @RequestParam Integer bookId) {
        return service.create(bookId, user);
    }

    @PutMapping("/{bookId}/rating")
    @ResponseStatus(HttpStatus.OK)
    public LibraryBookDto rate(@AuthenticationPrincipal User user,
                               @PathVariable Integer bookId,
                               @RequestParam Integer rating
    ) {
        return service.rate(bookId, user.getId(), rating);
    }

    @PutMapping("/{bookId}/status")
    @ResponseStatus(HttpStatus.OK)
    public LibraryBookDto updateStatus(@AuthenticationPrincipal User user,
                                       @PathVariable Integer bookId,
                                       @RequestParam LibraryBookStatus status
    ) {
        return service.updateStatus(bookId, user.getId(), status);
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Integer bookId) {
        service.delete(bookId, user.getId());
    }

}

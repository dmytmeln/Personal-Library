package org.example.library.library_book.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.library_book.domain.LibraryBookStatus;
import org.example.library.library_book.dto.LibraryBookDto;
import org.example.library.library_book.service.LibraryBookService;
import org.example.library.security.UserDetailsImpl;
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
    public Page<LibraryBookDto> getAll(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                       @RequestParam(defaultValue = "0") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size
    ) {
        return service.getAllByUserId(userDetails.getId(), PageRequest.of(page, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryBookDto create(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam Integer bookId) {
        return service.create(bookId, userDetails.user());
    }

    @PutMapping("/{bookId}/rating")
    @ResponseStatus(HttpStatus.OK)
    public LibraryBookDto rate(@AuthenticationPrincipal UserDetailsImpl userDetails,
                               @PathVariable Integer bookId,
                               @RequestParam Integer rating
    ) {
        return service.rate(bookId, userDetails.getId(), rating);
    }

    @PutMapping("/{bookId}/status")
    @ResponseStatus(HttpStatus.OK)
    public LibraryBookDto updateStatus(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                       @PathVariable Integer bookId,
                                       @RequestParam LibraryBookStatus status
    ) {
        return service.updateStatus(bookId, userDetails.getId(), status);
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Integer bookId) {
        service.delete(bookId, userDetails.getId());
    }

}

package org.example.library.library_book.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.library.book.dto.LanguageWithCount;
import org.example.library.library_book.domain.LibraryBookStatus;
import org.example.library.library_book.dto.*;
import org.example.library.library_book.service.LibraryBookService;
import org.example.library.pagination.PaginationParams;
import org.example.library.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/library-books")
@RequiredArgsConstructor
public class LibraryBookController {

    private final LibraryBookService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<LibraryBookDto> getAll(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                       PaginationParams paginationParams,
                                       LibraryBookSearchCriteria criteria
    ) {
        return service.getAllByUserId(userDetails.getId(), criteria, paginationParams);
    }

    @GetMapping("/languages")
    @ResponseStatus(HttpStatus.OK)
    public List<LanguageWithCount> getLanguages(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return service.getLanguagesByUserId(userDetails.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam Integer bookId) {
        service.create(bookId, userDetails.user());
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public void bulkAdd(@AuthenticationPrincipal UserDetailsImpl userDetails, @Valid @RequestBody BulkRequest request) {
        service.bulkAdd(request.getIds(), userDetails.user());
    }

    @PutMapping("/{libraryBookId}/rating")
    @ResponseStatus(HttpStatus.OK)
    public LibraryBookDto rate(@AuthenticationPrincipal UserDetailsImpl userDetails,
                               @PathVariable Integer libraryBookId,
                               @RequestParam Integer rating
    ) {
        return service.rate(libraryBookId, userDetails.getId(), rating);
    }

    @PutMapping("/{libraryBookId}/status")
    @ResponseStatus(HttpStatus.OK)
    public LibraryBookDto updateStatus(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                       @PathVariable Integer libraryBookId,
                                       @RequestParam LibraryBookStatus status
    ) {
        return service.updateStatus(libraryBookId, userDetails.getId(), status);
    }

    @PutMapping("/bulk-status")
    @ResponseStatus(HttpStatus.OK)
    public void bulkUpdateStatus(@AuthenticationPrincipal UserDetailsImpl userDetails, @Valid @RequestBody BulkStatusUpdateRequest request) {
        service.bulkUpdateStatus(request.getIds(), userDetails.getId(), request.getStatus());
    }

    @PutMapping("/{libraryBookId}/details")
    @ResponseStatus(HttpStatus.OK)
    public LibraryBookDto updateDetails(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable Integer libraryBookId,
                                         @Valid @RequestBody UpdateLibraryBookDetailsDto dto
    ) {
        return service.updateDetails(libraryBookId, userDetails.getId(), dto);
    }

    @PutMapping("/{libraryBookId}/details/reset")
    @ResponseStatus(HttpStatus.OK)
    public LibraryBookDto resetDetails(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                       @PathVariable Integer libraryBookId
    ) {
        return service.resetDetails(libraryBookId, userDetails.getId());
    }

    @DeleteMapping("/{libraryBookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Integer libraryBookId) {
        service.delete(libraryBookId, userDetails.getId());
    }

    @PostMapping("/bulk-remove")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkDelete(@AuthenticationPrincipal UserDetailsImpl userDetails, @Valid @RequestBody BulkRequest request) {
        service.bulkDelete(request.getIds(), userDetails.getId());
    }

}

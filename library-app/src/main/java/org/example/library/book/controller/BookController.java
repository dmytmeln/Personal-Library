package org.example.library.book.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.book.dto.BookDetails;
import org.example.library.book.dto.BookDto;
import org.example.library.book.dto.BookSearchParams;
import org.example.library.book.service.BookDetailsService;
import org.example.library.book.service.BookService;
import org.example.library.pagination.PaginationParams;
import org.example.library.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;
    private final BookDetailsService bookDetailsService;


    @GetMapping
    public Page<BookDto> getAllBooks(PaginationParams paginationParams, BookSearchParams searchParams) {
        return service.getAll(paginationParams, searchParams);
    }

    @GetMapping("/{bookId}/details")
    public BookDetails getById(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Integer bookId) {
        return bookDetailsService.getDetails(bookId, userDetails.getId());
    }

}

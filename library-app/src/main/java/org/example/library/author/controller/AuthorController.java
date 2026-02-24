package org.example.library.author.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.author.dto.AuthorDto;
import org.example.library.author.dto.AuthorSearchParams;
import org.example.library.author.dto.AuthorWithBooksCount;
import org.example.library.author.dto.CountryWithCount;
import org.example.library.author.service.AuthorService;
import org.example.library.pagination.PaginationParams;
import org.example.library.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService service;

    @GetMapping
    public Page<AuthorWithBooksCount> getAll(
            PaginationParams paginationParams,
            AuthorSearchParams searchParams
    ) {
        return service.search(paginationParams, searchParams);
    }

    @GetMapping("/me")
    public Page<AuthorWithBooksCount> getAllForUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            PaginationParams paginationParams,
            AuthorSearchParams searchParams
    ) {
        return service.searchForUser(userDetails.getId(), paginationParams, searchParams);
    }

    @GetMapping("/{authorId}")
    public AuthorDto getById(@PathVariable Integer authorId) {
        return service.getById(authorId);
    }

    @GetMapping("/countries")
    public List<CountryWithCount> getAllCountries() {
        return service.getAllCountries();
    }

    @GetMapping("/countries/me")
    public List<CountryWithCount> getCountriesForUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return service.getCountriesForUser(userDetails.getId());
    }

}

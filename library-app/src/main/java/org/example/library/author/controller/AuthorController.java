package org.example.library.author.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.author.dto.AuthorDto;
import org.example.library.author.service.AuthorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService service;

    @GetMapping("/{authorId}")
    public AuthorDto getById(@PathVariable("authorId") Integer authorId) {
        return service.getById(authorId);
    }

}

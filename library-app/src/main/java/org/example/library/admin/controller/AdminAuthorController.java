package org.example.library.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.library.author.dto.AuthorResponse;
import org.example.library.author.dto.AuthorSaveRequest;
import org.example.library.author.service.AuthorService;
import org.example.library.library_book.dto.BulkRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/v1/admin/authors")
@RequiredArgsConstructor
public class AdminAuthorController {

    private final AuthorService authorService;

    @GetMapping("/{id}")
    public AuthorResponse getAuthor(@PathVariable Integer id) {
        return authorService.getAuthorWithAllTranslations(id);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public AuthorResponse createAuthor(@Valid @RequestBody AuthorSaveRequest request) {
        return authorService.saveAuthor(request);
    }

    @PutMapping("/{id}")
    public AuthorResponse updateAuthor(@PathVariable Integer id, @Valid @RequestBody AuthorSaveRequest request) {
        return authorService.updateAuthor(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteAuthor(@PathVariable Integer id) {
        authorService.deleteAuthor(id);
    }

    @PostMapping("/bulk-delete")
    @ResponseStatus(NO_CONTENT)
    public void deleteAuthors(@Valid @RequestBody BulkRequest request) {
        authorService.deleteAuthors(request.getIds());
    }

}

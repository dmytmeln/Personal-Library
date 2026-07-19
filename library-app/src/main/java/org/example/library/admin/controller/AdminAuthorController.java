package org.example.library.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.admin.dto.AdminAuthorDto;
import org.example.library.admin.service.AdminAuthorService;
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

    private final AdminAuthorService adminAuthorService;

    @GetMapping("/{id}")
    public AdminAuthorDto getAuthor(@PathVariable Integer id) {
        return adminAuthorService.getAuthor(id);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public void createAuthor(@RequestBody AdminAuthorDto dto) {
        adminAuthorService.createAuthor(dto);
    }

    @PutMapping("/{id}")
    public void updateAuthor(@PathVariable Integer id, @RequestBody AdminAuthorDto dto) {
        adminAuthorService.updateAuthor(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteAuthor(@PathVariable Integer id) {
        adminAuthorService.deleteAuthor(id);
    }

    @PostMapping("/bulk-delete")
    @ResponseStatus(NO_CONTENT)
    public void deleteAuthors(@RequestBody BulkRequest request) {
        adminAuthorService.deleteAuthors(request.getIds());
    }

}

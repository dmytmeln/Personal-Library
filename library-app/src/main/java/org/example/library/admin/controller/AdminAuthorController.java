package org.example.library.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.admin.dto.AdminAuthorDto;
import org.example.library.admin.service.AdminAuthorService;
import org.example.library.library_book.dto.BulkRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    @ResponseStatus(HttpStatus.CREATED)
    public void createAuthor(@RequestBody AdminAuthorDto dto) {
        adminAuthorService.createAuthor(dto);
    }

    @PutMapping("/{id}")
    public void updateAuthor(@PathVariable Integer id, @RequestBody AdminAuthorDto dto) {
        adminAuthorService.updateAuthor(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable Integer id) {
        adminAuthorService.deleteAuthor(id);
    }

    @PostMapping("/bulk-delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthors(@RequestBody BulkRequest request) {
        adminAuthorService.deleteAuthors(request.getIds());
    }

}

package org.example.library.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.admin.dto.AdminBookDto;
import org.example.library.admin.service.AdminBookService;
import org.example.library.library_book.dto.BulkRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

    private final AdminBookService adminBookService;

    @GetMapping("/{id}")
    public AdminBookDto getBook(@PathVariable Integer id) {
        return adminBookService.getBook(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createBook(@RequestBody AdminBookDto dto) {
        adminBookService.createBook(dto);
    }

    @PutMapping("/{id}")
    public void updateBook(@PathVariable Integer id, @RequestBody AdminBookDto dto) {
        adminBookService.updateBook(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Integer id) {
        adminBookService.deleteBook(id);
    }

    @PostMapping("/bulk-delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooks(@RequestBody BulkRequest request) {
        adminBookService.deleteBooks(request.getIds());
    }

}

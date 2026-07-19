package org.example.library.collection_book.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.library.collection_book.dto.CollectionBookSearchParams;
import org.example.library.collection_book.service.CollectionBookService;
import org.example.library.common.pagination.PaginationParams;
import org.example.library.library_book.dto.BulkRequest;
import org.example.library.library_book.dto.LibraryBookDto;
import org.example.library.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/collections/{collectionId}/books")
@RequiredArgsConstructor
public class CollectionBookController {

    private final CollectionBookService service;

    @GetMapping
    @ResponseStatus(OK)
    public Page<LibraryBookDto> getCollectionBooks(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                   @PathVariable int collectionId,
                                                   CollectionBookSearchParams searchParams,
                                                   PaginationParams paginationParams) {
        return service.getCollectionBooksPaginated(userPrincipal.getId(), collectionId, searchParams, paginationParams);
    }

    @PostMapping("/{libraryBookId}")
    @ResponseStatus(CREATED)
    public void addBookToCollection(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                    @PathVariable int collectionId,
                                    @PathVariable int libraryBookId) {
        service.addBookToCollection(userPrincipal.getId(), collectionId, libraryBookId);
    }

    @PostMapping("/bulk")
    @ResponseStatus(CREATED)
    public void bulkAddBooksToCollection(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                         @PathVariable int collectionId,
                                         @Valid @RequestBody BulkRequest request) {
        service.bulkAddBooksToCollection(userPrincipal.getId(), collectionId, request.getIds());
    }

    @DeleteMapping("/{libraryBookId}")
    @ResponseStatus(NO_CONTENT)
    public void removeBookFromCollection(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                         @PathVariable int collectionId,
                                         @PathVariable int libraryBookId) {
        service.removeBookFromCollection(userPrincipal.getId(), collectionId, libraryBookId);
    }

    @PostMapping("/bulk-remove")
    @ResponseStatus(NO_CONTENT)
    public void bulkRemoveBooksFromCollection(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                              @PathVariable int collectionId,
                                              @Valid @RequestBody BulkRequest request) {
        service.bulkRemoveBooksFromCollection(userPrincipal.getId(), collectionId, request.getIds());
    }

}

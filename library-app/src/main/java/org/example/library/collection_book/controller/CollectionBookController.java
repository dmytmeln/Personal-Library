package org.example.library.collection_book.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.library.collection_book.dto.CollectionBookDto;
import org.example.library.collection_book.dto.CollectionBookSearchParams;
import org.example.library.collection_book.service.CollectionBookService;
import org.example.library.library_book.dto.BulkLibraryBookRequest;
import org.example.library.library_book.dto.LibraryBookDto;
import org.example.library.pagination.PaginationParams;
import org.example.library.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/collections/{collectionId}/books")
@RequiredArgsConstructor
public class CollectionBookController {

    private final CollectionBookService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<LibraryBookDto> getCollectionBooks(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable int collectionId,
            CollectionBookSearchParams searchParams,
            PaginationParams paginationParams
    ) {
        return service.getCollectionBooksPaginated(userDetails.getId(), collectionId, searchParams, paginationParams);
    }

    @PostMapping("/{libraryBookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionBookDto addBookToCollection(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @PathVariable int collectionId,
                                                  @PathVariable int libraryBookId
    ) {
        return service.addBookToCollection(userDetails.getId(), collectionId, libraryBookId);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public void bulkAddBooksToCollection(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable int collectionId,
                                         @Valid @RequestBody BulkLibraryBookRequest request
    ) {
        service.bulkAddBooksToCollection(userDetails.getId(), collectionId, request.getIds());
    }

    @DeleteMapping("/{libraryBookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookFromCollection(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @PathVariable int collectionId,
                                          @PathVariable int libraryBookId
    ) {
        service.removeBookFromCollection(userDetails.getId(), collectionId, libraryBookId);
    }

    @PostMapping("/bulk-remove")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkRemoveBooksFromCollection(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @PathVariable int collectionId,
                                              @Valid @RequestBody BulkLibraryBookRequest request
    ) {
        service.bulkRemoveBooksFromCollection(userDetails.getId(), collectionId, request.getIds());
    }

    @PostMapping("/{libraryBookId}/move/{toCollectionId}")
    @ResponseStatus(HttpStatus.OK)
    public CollectionBookDto moveBook(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @PathVariable int collectionId,
                                      @PathVariable int libraryBookId,
                                      @PathVariable int toCollectionId) {
        return service.moveBook(userDetails.getId(), libraryBookId, collectionId, toCollectionId);
    }

}

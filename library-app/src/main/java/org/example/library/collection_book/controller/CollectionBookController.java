package org.example.library.collection_book.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.collection_book.dto.CollectionBookDto;
import org.example.library.collection_book.service.CollectionBookService;
import org.example.library.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collections/{collectionId}/books")
@RequiredArgsConstructor
public class CollectionBookController {

    private final CollectionBookService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CollectionBookDto> getCollectionBooks(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable int collectionId) {
        return service.getCollectionBooks(userDetails.getId(), collectionId);
    }

    @PostMapping("/{bookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionBookDto addBookToCollection(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @PathVariable int collectionId,
                                                  @PathVariable int bookId
    ) {
        return service.addBookToCollection(userDetails.getId(), collectionId, bookId);
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookFromCollection(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @PathVariable int collectionId,
                                          @PathVariable int bookId
    ) {
        service.removeBookFromCollection(userDetails.getId(), collectionId, bookId);
    }

}

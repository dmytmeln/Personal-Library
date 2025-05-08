package org.example.library.collection_book.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.collection_book.dto.CollectionBookDto;
import org.example.library.collection_book.service.CollectionBookService;
import org.example.library.user.domain.User;
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
    public List<CollectionBookDto> getCollectionBooks(@AuthenticationPrincipal User user, @PathVariable int collectionId) {
        return service.getCollectionBooks(user.getId(), collectionId);
    }

    @PostMapping("/{bookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionBookDto addBookToCollection(@AuthenticationPrincipal User user,
                                                 @PathVariable int collectionId,
                                                 @PathVariable int bookId
    ) {
        return service.addBookToCollection(user.getId(), collectionId, bookId);
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookFromCollection(@AuthenticationPrincipal User user,
                                         @PathVariable int collectionId,
                                         @PathVariable int bookId
    ) {
        service.removeBookFromCollection(user.getId(), collectionId, bookId);
    }

}

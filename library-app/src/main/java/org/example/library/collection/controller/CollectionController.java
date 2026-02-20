
package org.example.library.collection.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.collection.dto.*;
import org.example.library.collection.service.CollectionService;
import org.example.library.collection_book.service.CollectionBookService;
import org.example.library.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService service;
    private final CollectionBookService collectionBookService;


    @GetMapping("/tree")
    @ResponseStatus(HttpStatus.OK)
    public List<CollectionNodeDto> getTree(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return service.getUserCollectionTree(userDetails.getId());
    }

    @GetMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.OK)
    public CollectionDetailsDto getDetails(@PathVariable Integer collectionId,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return service.getCollectionDetails(collectionId, userDetails.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BasicCollectionDto create(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @RequestBody CreateCollectionRequest dto) {
        return service.createCollection(dto, userDetails.getId());
    }

    @PutMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.OK)
    public BasicCollectionDto update(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @PathVariable Integer collectionId,
                                     @RequestBody UpdateCollectionDto dto) {
        return service.updateCollection(collectionId, dto, userDetails.getId());
    }

    @PatchMapping("/{collectionId}/parent")
    @ResponseStatus(HttpStatus.OK)
    public void moveCollection(@AuthenticationPrincipal UserDetailsImpl userDetails,
                               @PathVariable Integer collectionId,
                               @RequestBody MoveCollectionRequest request) {
        service.moveCollection(collectionId, request.getNewParentId(), userDetails.getId());
    }

    @PatchMapping("/bulk-move")
    @ResponseStatus(HttpStatus.OK)
    public void bulkMoveCollections(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @RequestBody BulkMoveRequest request) {
        service.bulkMoveCollections(request.getCollectionIdsToMove(), request.getNewParentId(), userDetails.getId());
    }

    @DeleteMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollection(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable Integer collectionId) {
        service.deleteCollection(collectionId, userDetails.getId());
    }

    @PostMapping("/books")
    @ResponseStatus(HttpStatus.OK)
    public void addBookToCollections(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                     @RequestBody AddBookToCollectionsRequest request) {
        service.addBookToCollections(request.getLibraryBookId(), request.getCollectionIds(), userDetails.getId());
    }

    @PatchMapping("/{sourceCollectionId}/books/{bookId}/move")
    @ResponseStatus(HttpStatus.OK)
    public void moveBookBetweenCollections(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @PathVariable Integer sourceCollectionId,
                                           @PathVariable Integer bookId,
                                           @RequestBody MoveCollectionRequest request) {
        service.moveBook(sourceCollectionId, request.getNewParentId(), bookId, userDetails.getId());
    }

    @DeleteMapping("/books")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookFromAllCollections(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @RequestParam Integer libraryBookId) {
        collectionBookService.removeBookFromAllCollections(userDetails.getId(), libraryBookId);
    }

}

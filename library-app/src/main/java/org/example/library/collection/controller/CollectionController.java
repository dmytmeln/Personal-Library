
package org.example.library.collection.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.library.collection.dto.BasicCollectionDto;
import org.example.library.collection.dto.CollectionDetailsDto;
import org.example.library.collection.dto.CollectionNodeDto;
import org.example.library.collection.dto.CreateCollectionRequest;
import org.example.library.collection.dto.MoveCollectionRequest;
import org.example.library.collection.dto.UpdateCollectionDto;
import org.example.library.collection.service.CollectionService;
import org.example.library.collection_book.service.CollectionBookService;
import org.example.library.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService service;
    private final CollectionBookService collectionBookService;

    @GetMapping("/tree")
    @ResponseStatus(HttpStatus.OK)
    public List<CollectionNodeDto> getTree(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return service.getUserCollectionHierarchy(userPrincipal.getId());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BasicCollectionDto> getAll(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                           @RequestParam Integer libraryBookId) {
        return service.getCollectionsContainingLibraryBook(userPrincipal.getId(), libraryBookId);
    }

    @GetMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.OK)
    public CollectionDetailsDto getDetails(@PathVariable Integer collectionId,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return service.getCollectionDetails(collectionId, userPrincipal.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BasicCollectionDto create(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                     @Valid @RequestBody CreateCollectionRequest dto) {
        return service.createCollection(dto, userPrincipal.getId());
    }

    @PutMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.OK)
    public BasicCollectionDto update(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                     @PathVariable Integer collectionId,
                                     @Valid @RequestBody UpdateCollectionDto dto) {
        return service.updateCollection(collectionId, dto, userPrincipal.getId());
    }

    @PatchMapping("/{collectionId}/move")
    @ResponseStatus(HttpStatus.OK)
    public void moveCollection(@AuthenticationPrincipal UserPrincipal userPrincipal,
                               @PathVariable Integer collectionId,
                               @Valid @RequestBody MoveCollectionRequest request) {
        service.moveCollection(collectionId, request.getNewParentId(), userPrincipal.getId());
    }

    @DeleteMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollection(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                 @PathVariable Integer collectionId) {
        service.deleteCollection(collectionId, userPrincipal.getId());
    }

    @PatchMapping("/{sourceCollectionId}/books/{bookId}/move")
    @ResponseStatus(HttpStatus.OK)
    public void moveBookBetweenCollections(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                           @PathVariable Integer sourceCollectionId,
                                           @PathVariable Integer bookId,
                                           @Valid @RequestBody MoveCollectionRequest request) {
        service.moveBook(sourceCollectionId, request.getNewParentId(), bookId, userPrincipal.getId());
    }

    @DeleteMapping("/books")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookFromAllCollections(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                             @RequestParam Integer libraryBookId) {
        collectionBookService.removeBookFromAllCollections(userPrincipal.getId(), libraryBookId);
    }

}

package org.example.library.collection.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.collection.dto.CollectionDto;
import org.example.library.collection.dto.CreateCollectionRequest;
import org.example.library.collection.service.CollectionService;
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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CollectionDto> getAll(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @RequestParam(required = false) Integer libraryBookId) {
        if (libraryBookId != null)
            return service.getAllByUserIdAndLibraryBookId(userDetails.getId(), libraryBookId);

        return service.getAllByUserId(userDetails.getId());
    }

    @GetMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.OK)
    public CollectionDto get(@PathVariable Integer collectionId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return service.getById(userDetails.getId(), collectionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionDto create(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody CreateCollectionRequest dto) {
        return service.createCollection(dto, userDetails.user());
    }

    @PutMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.OK)
    public CollectionDto update(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @PathVariable Integer collectionId,
                                @RequestBody CreateCollectionRequest dto
    ) {
        return service.updateCollection(collectionId, dto, userDetails.getId());
    }

}

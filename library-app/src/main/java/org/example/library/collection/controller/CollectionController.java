package org.example.library.collection.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.collection.dto.CollectionDto;
import org.example.library.collection.dto.CreateCollectionRequest;
import org.example.library.collection.service.CollectionService;
import org.example.library.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CollectionDto> getAll(@AuthenticationPrincipal User user) {
        return service.getAllByUserId(user.getId());
    }

    @GetMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.OK)
    public CollectionDto get(@PathVariable Integer collectionId, @AuthenticationPrincipal User user) {
        return service.getById(user.getId(), collectionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionDto create(@AuthenticationPrincipal User user, @RequestBody CreateCollectionRequest dto) {
        return service.createCollection(dto, user);
    }

}

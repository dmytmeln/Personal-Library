package org.example.library.note.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.library.note.dto.NoteDto;
import org.example.library.note.dto.NoteRequest;
import org.example.library.note.service.NoteService;
import org.example.library.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService service;


    @GetMapping
    public NoteDto getByLibraryBookId(@RequestParam Integer libraryBookId,
                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return service.getByLibraryBookId(libraryBookId, userDetails.getId());
    }

    @PutMapping
    public NoteDto createOrUpdate(@Valid @RequestBody NoteRequest request,
                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return service.createOrUpdate(request, userDetails.getId());
    }

    @DeleteMapping
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@RequestParam Integer libraryBookId,
                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        service.delete(libraryBookId, userDetails.getId());
    }

}

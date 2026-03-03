package org.example.library.note.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.exception.NotFoundException;
import org.example.library.library_book.repository.LibraryBookRepository;
import org.example.library.note.domain.Note;
import org.example.library.note.dto.NoteDto;
import org.example.library.note.dto.NoteRequest;
import org.example.library.note.mapper.NoteMapper;
import org.example.library.note.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {

    private final NoteRepository repository;
    private final NoteMapper mapper;
    private final LibraryBookRepository libraryBookRepository;


    @Transactional(readOnly = true)
    public NoteDto getByLibraryBookId(Integer libraryBookId, Integer userId) {
        return repository.findByLibraryBookIdAndLibraryBookUserId(libraryBookId, userId)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("error.note.not_found"));
    }

    @Transactional
    public NoteDto createOrUpdate(NoteRequest request, Integer userId) {
        return repository.findByLibraryBookIdAndLibraryBookUserId(request.libraryBookId(), userId)
                .map(existingNote -> updateExisting(existingNote, request))
                .orElseGet(() -> createNew(request, userId));
    }

    @Transactional
    public void delete(Integer libraryBookId, Integer userId) {
        repository.deleteByLibraryBookIdAndLibraryBookUserId(libraryBookId, userId);
        log.info("[NOTE_DELETE] User ID: {}, Library Book ID: {}", userId, libraryBookId);
    }

    private NoteDto createNew(NoteRequest request, Integer userId) {
        var libraryBook = libraryBookRepository.findByIdAndUserId(request.libraryBookId(), userId)
                .orElseThrow(() -> new NotFoundException("error.library_book.not_found"));
        var note = mapper.toEntity(request);
        note.setLibraryBook(libraryBook);
        var savedNote = repository.saveAndFlush(note);
        log.info("[NOTE_CREATE] User ID: {}, Library Book ID: {}", userId, request.libraryBookId());
        return mapper.toDto(savedNote);
    }

    private NoteDto updateExisting(Note existingNote, NoteRequest request) {
        mapper.update(existingNote, request);
        var savedNote = repository.saveAndFlush(existingNote);
        log.info("[NOTE_UPDATE] User ID: {}, Library Book ID: {}", existingNote.getLibraryBook().getUser().getId(), existingNote.getLibraryBook().getId());
        return mapper.toDto(savedNote);
    }

}

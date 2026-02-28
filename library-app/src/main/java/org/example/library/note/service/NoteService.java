package org.example.library.note.service;

import lombok.RequiredArgsConstructor;
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
    }

    private NoteDto createNew(NoteRequest request, Integer userId) {
        var libraryBook = libraryBookRepository.findByIdAndUserId(request.libraryBookId(), userId)
                .orElseThrow(() -> new NotFoundException("error.library_book.not_found"));
        var note = mapper.toEntity(request);
        note.setLibraryBook(libraryBook);
        return mapper.toDto(repository.saveAndFlush(note));
    }

    private NoteDto updateExisting(Note existingNote, NoteRequest request) {
        mapper.update(existingNote, request);
        return mapper.toDto(repository.saveAndFlush(existingNote));
    }

}

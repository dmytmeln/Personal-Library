package org.example.library.author.service;

import lombok.RequiredArgsConstructor;
import org.example.library.author.domain.Author;
import org.example.library.author.dto.AuthorDto;
import org.example.library.author.mapper.AuthorMapper;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.exception.NotFoundException;
import org.example.library.pagination.PaginationParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository repository;
    private final AuthorMapper mapper;

    public Page<AuthorDto> search(String name, PaginationParams paginationParams) {
        var pageable = PageRequest.of(paginationParams.getPage(), paginationParams.getSize());
        if (name == null || name.isBlank()) {
            return repository.findAll(pageable).map(mapper::toDto);
        }
        return repository.findByFullNameContainingIgnoreCase(name, pageable).map(mapper::toDto);
    }

    public Author getExistingById(Integer authorId) {
        return repository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Author not found"));
    }

    public AuthorDto getById(Integer authorId) {
        return mapper.toDto(getExistingById(authorId));
    }

}

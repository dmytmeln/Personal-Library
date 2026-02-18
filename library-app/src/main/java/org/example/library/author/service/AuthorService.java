package org.example.library.author.service;

import lombok.RequiredArgsConstructor;
import org.example.library.author.domain.Author;
import org.example.library.author.dto.AuthorDto;
import org.example.library.author.dto.AuthorSearchParams;
import org.example.library.author.dto.AuthorWithBooksCount;
import org.example.library.author.mapper.AuthorMapper;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.exception.NotFoundException;
import org.example.library.pagination.PageRequestBuilder;
import org.example.library.pagination.PaginationParams;
import org.example.library.pagination.SortableFields;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository repository;
    private final AuthorMapper mapper;
    private final PageRequestBuilder pageRequestBuilder;

    public Page<AuthorWithBooksCount> search(PaginationParams paginationParams, AuthorSearchParams searchParams) {
        var pageable = pageRequestBuilder.buildPageRequest(paginationParams, SortableFields.AUTHOR_FIELDS);
        return repository.searchWithBooksCount(searchParams.getName(), pageable);
    }

    public Author getExistingById(Integer authorId) {
        return repository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Author not found"));
    }

    public AuthorDto getById(Integer authorId) {
        return mapper.toDto(getExistingById(authorId));
    }

}

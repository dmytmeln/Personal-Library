package org.example.library.author.service;

import lombok.RequiredArgsConstructor;
import org.example.library.author.dto.AuthorDto;
import org.example.library.author.dto.AuthorSearchParams;
import org.example.library.author.dto.AuthorWithBooksCount;
import org.example.library.author.dto.CountryWithCount;
import org.example.library.author.mapper.AuthorMapper;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.common.exception.NotFoundException;
import org.example.library.common.pagination.PageRequestBuilder;
import org.example.library.common.pagination.PaginationParams;
import org.example.library.common.pagination.SortableFields;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository repository;
    private final AuthorMapper mapper;
    private final PageRequestBuilder pageRequestBuilder;

    public Page<AuthorWithBooksCount> search(PaginationParams paginationParams, AuthorSearchParams searchParams) {
        var pageable = pageRequestBuilder.buildPageRequest(paginationParams, SortableFields.AUTHOR_FIELDS);
        var languageCode = getLanguageCode();

        return repository.searchWithBooksCount(searchParams, languageCode, pageable);
    }

    public AuthorDto getById(Integer authorId) {
        var languageCode = getLanguageCode();
        return repository.findDisplayViewByIdAndLanguageCode(authorId, languageCode)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("error.author.not_found"));
    }

    public List<CountryWithCount> getAllCountries() {
        var languageCode = getLanguageCode();
        return repository.findAllCountriesWithCount(languageCode);
    }

    public Page<AuthorWithBooksCount> searchForUser(Integer userId, PaginationParams paginationParams, AuthorSearchParams searchParams) {
        var pageable = pageRequestBuilder.buildPageRequest(paginationParams, SortableFields.AUTHOR_FIELDS);
        var languageCode = getLanguageCode();
        return repository.searchForUser(userId, searchParams, languageCode, pageable);
    }

    public List<CountryWithCount> getCountriesForUser(Integer userId) {
        var languageCode = getLanguageCode();
        return repository.findAllCountriesForUser(userId, languageCode);
    }

    private String getLanguageCode() {
        return LocaleContextHolder.getLocale().getLanguage();
    }

}

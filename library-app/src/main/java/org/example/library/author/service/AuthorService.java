package org.example.library.author.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.author.domain.Author;
import org.example.library.author.dto.AuthorDto;
import org.example.library.author.dto.AuthorResponse;
import org.example.library.author.dto.AuthorSaveRequest;
import org.example.library.author.dto.AuthorSearchParams;
import org.example.library.author.dto.AuthorWithBooksCount;
import org.example.library.author.dto.CountryWithCount;
import org.example.library.author.mapper.AuthorMapper;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.book.repository.BookRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.example.library.common.localization.DefaultLanguage;
import org.example.library.common.pagination.PageRequestBuilder;
import org.example.library.common.pagination.PaginationParams;
import org.example.library.common.pagination.SortableFields;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorService {

    private static final String AUTHOR_NOT_FOUND_ERROR_MSG = "error.author.not_found";
    private static final String AUTHOR_HAS_BOOKS_ERROR_MSG = "error.author.has_books";
    private static final String DEFAULT_TRANSLATION_MISSING_ERROR_MSG = "error.author.default_translation_missing";

    private final AuthorRepository repository;
    private final BookRepository bookRepository;
    private final AuthorMapper mapper;
    private final PageRequestBuilder pageRequestBuilder;
    private final DefaultLanguage defaultLanguage;

    @Transactional
    public AuthorResponse saveAuthor(AuthorSaveRequest dto) {
        validateDefaultTranslation(dto);

        var author = new Author();
        updateAuthorFields(author, dto);

        var savedAuthor = repository.save(author);
        log.info("[ADMIN_AUTHOR_CREATE] Author ID: {}", savedAuthor.getId());
        return mapper.toAuthorResponse(savedAuthor);
    }

    @Transactional(readOnly = true)
    public AuthorDto getLocalizedAuthor(Integer authorId) {
        var author = repository.findById(authorId)
                .orElseThrow(() -> new NotFoundException(AUTHOR_NOT_FOUND_ERROR_MSG));
        return mapper.toDto(author, getCurrentLanguageCode(), defaultLanguage);
    }

    @Transactional(readOnly = true)
    public AuthorResponse getAuthorWithAllTranslations(Integer id) {
        var author = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(AUTHOR_NOT_FOUND_ERROR_MSG));

        return mapper.toAuthorResponse(author);
    }

    @Transactional(readOnly = true)
    public Page<AuthorWithBooksCount> searchInCatalog(PaginationParams paginationParams, AuthorSearchParams searchParams) {
        var pageable = pageRequestBuilder.buildPageRequest(paginationParams, SortableFields.AUTHOR_FIELDS);
        return repository.searchWithBooksCount(searchParams, getCurrentLanguageCode(), defaultLanguage.code(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuthorWithBooksCount> searchInUserLibrary(Integer userId,
                                                          PaginationParams paginationParams,
                                                          AuthorSearchParams searchParams) {
        var pageable = pageRequestBuilder.buildPageRequest(paginationParams, SortableFields.AUTHOR_FIELDS);
        return repository.searchForUser(userId, searchParams, getCurrentLanguageCode(), defaultLanguage.code(), pageable);
    }

    @Transactional(readOnly = true)
    public List<CountryWithCount> getAuthorCountriesWithCount() {
        return repository.findAllAuthorCountriesWithCount(getCurrentLanguageCode(), defaultLanguage.code());
    }

    @Transactional(readOnly = true)
    public List<CountryWithCount> getUserAuthorCountriesWithCount(Integer userId) {
        return repository.findAllAuthorCountriesWithCountForUser(userId, getCurrentLanguageCode(), defaultLanguage.code());
    }

    @Transactional
    public AuthorResponse updateAuthor(Integer id, AuthorSaveRequest dto) {
        validateDefaultTranslation(dto);

        var author = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(AUTHOR_NOT_FOUND_ERROR_MSG));
        updateAuthorFields(author, dto);

        var savedAuthor = repository.save(author);
        log.info("[ADMIN_AUTHOR_UPDATE] Author ID: {}", id);
        return mapper.toAuthorResponse(savedAuthor);
    }

    @Transactional
    public void deleteAuthor(Integer id) {
        var author = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(AUTHOR_NOT_FOUND_ERROR_MSG));

        if (bookRepository.existsByAuthorsId(id)) {
            throw new BadRequestException(AUTHOR_HAS_BOOKS_ERROR_MSG);
        }

        repository.delete(author);
        log.info("[ADMIN_AUTHOR_DELETE] Author ID: {}", id);
    }

    @Transactional
    public void deleteAuthors(List<Integer> ids) {
        if (bookRepository.existsByAuthorsIdIn(ids)) {
            throw new BadRequestException(AUTHOR_HAS_BOOKS_ERROR_MSG);
        }

        repository.deleteAllByIdInBatch(ids);
        log.info("[ADMIN_AUTHORS_BULK_DELETE] Count: {}", ids.size());
    }

    private void validateDefaultTranslation(AuthorSaveRequest dto) {
        if (dto == null || dto.getTranslations() == null || !dto.getTranslations().containsKey(defaultLanguage.code())) {
            throw new BadRequestException(DEFAULT_TRANSLATION_MISSING_ERROR_MSG);
        }
    }

    private void updateAuthorFields(Author author, AuthorSaveRequest dto) {
        author.setBirthYear(dto.getBirthYear());
        author.setDeathYear(dto.getDeathYear());

        author.syncTranslations(dto.getTranslations());
    }

    private String getCurrentLanguageCode() {
        return LocaleContextHolder.getLocale().getLanguage();
    }

}

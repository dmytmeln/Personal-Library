package org.example.library.book.service;

import lombok.RequiredArgsConstructor;
import org.example.library.book.dto.BookDto;
import org.example.library.book.dto.BookSearchParams;
import org.example.library.book.dto.LanguageWithCount;
import org.example.library.book.mapper.BookMapper;
import org.example.library.book.repository.BookDisplayViewRepository;
import org.example.library.book.repository.BookRepository;
import org.example.library.book.repository.BookSpecification;
import org.example.library.exception.NotFoundException;
import org.example.library.pagination.PageRequestBuilder;
import org.example.library.pagination.PaginationParams;
import org.example.library.pagination.SortableFields;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;
    private final BookDisplayViewRepository displayViewRepository;
    private final BookMapper mapper;
    private final PageRequestBuilder pageRequestBuilder;


    public Page<BookDto> getAll(PaginationParams paginationParams, BookSearchParams searchParams) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        var spec = BookSpecification.fromSearchParams(lang, searchParams);
        var pageable = pageRequestBuilder.buildPageRequest(paginationParams, SortableFields.BOOK_FIELDS);

        return displayViewRepository.findAll(spec, pageable)
                .map(mapper::toBookDto);
    }

    public void verifyExistsById(Integer bookId) {
        if (!repository.existsById(bookId)) {
            throw new NotFoundException("error.book.not_found");
        }
    }

    public BookDto getById(Integer id) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        return displayViewRepository.findByIdAndLanguageCode(id, lang)
                .map(mapper::toBookDto)
                .orElseThrow(() -> new NotFoundException("error.book.not_found"));
    }

    public List<LanguageWithCount> getAllLanguages() {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        return repository.findAllLanguagesWithCount(lang);
    }

}

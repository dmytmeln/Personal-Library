package org.example.library.book.service;

import lombok.RequiredArgsConstructor;
import org.example.library.book.domain.Book;
import org.example.library.book.dto.BookDto;
import org.example.library.book.dto.BookSearchParams;
import org.example.library.book.mapper.BookMapper;
import org.example.library.book.repository.BookRepository;
import org.example.library.book.repository.BookSpecification;
import org.example.library.exception.NotFoundException;
import org.example.library.pagination.PageRequestBuilder;
import org.example.library.pagination.PaginationParams;
import org.example.library.pagination.SortableFields;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;
    private final BookMapper mapper;
    private final PageRequestBuilder pageRequestBuilder;


    public Page<BookDto> getAll(PaginationParams paginationParams, BookSearchParams searchParams) {
        var spec = BookSpecification.fromSearchParams(searchParams);
        var pageable = pageRequestBuilder.buildPageRequest(paginationParams, SortableFields.BOOK_FIELDS);

        return repository.findAll(spec, pageable)
                .map(mapper::toBookDto);
    }

    public Book getExistingById(Integer bookId) {
        return repository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found"));
    }

    public void verifyExistsById(Integer bookId) {
        if (!repository.existsById(bookId)) {
            throw new NotFoundException("Book not found");
        }
    }

}

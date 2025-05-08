package org.example.library.book.service;

import lombok.RequiredArgsConstructor;
import org.example.library.book.domain.Book;
import org.example.library.book.dto.BookDto;
import org.example.library.book.mapper.BookMapper;
import org.example.library.book.repository.BookRepository;
import org.example.library.book.repository.BookSpecification;
import org.example.library.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;
    private final BookMapper mapper;

    public Page<BookDto> getAll(Integer page, Integer size, Integer categoryId, Integer authorId) {
        var spec = Specification.where(BookSpecification.hasCategoryId(categoryId))
                .and(BookSpecification.hasAuthorId(authorId));
        var pageReq = authorId == null
                ? PageRequest.of(page, size)
                : Pageable.unpaged();
        return repository.findAll(spec, pageReq)
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

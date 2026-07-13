package org.example.library.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.admin.dto.AdminBookDto;
import org.example.library.author.domain.Author;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookStatus;
import org.example.library.book.domain.BookTranslation;
import org.example.library.book.repository.BookRepository;
import org.example.library.category.repository.CategoryRepository;
import org.example.library.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public AdminBookDto getBook(Integer id) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("error.book.not_found"));

        return AdminBookDto.builder()
                .id(book.getId())
                .categoryId(book.getCategory() != null ? book.getCategory().getId() : null)
                .publishYear(book.getPublishYear())
                .pages(book.getPages())
                .coverImageUrl(book.getCoverImageUrl())
                .authorIds(book.getAuthors().stream().map(Author::getId).toList())
                .translations(book.getTranslations().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> AdminBookDto.AdminBookTranslationDto.builder()
                                .title(e.getValue().getTitle())
                                .bookLanguage(e.getValue().getBookLanguage())
                                .description(e.getValue().getDescription())
                                .build())))
                .build();
    }

    @Transactional
    public void createBook(AdminBookDto dto) {
        var book = new Book();
        updateBookFields(book, dto);

        var savedBook = bookRepository.save(book);
        log.info("[ADMIN_BOOK_CREATE] Book ID: {}", savedBook.getId());
    }

    @Transactional
    public void updateBook(Integer id, AdminBookDto dto) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("error.book.not_found"));
        updateBookFields(book, dto);

        bookRepository.save(book);
        log.info("[ADMIN_BOOK_UPDATE] Book ID: {}", id);
    }

    @Transactional
    public void deleteBook(Integer id) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("error.book.not_found"));

        bookRepository.delete(book);
        log.info("[ADMIN_BOOK_DELETE] Book ID: {}", id);
    }

    @Transactional
    public void deleteBooks(List<Integer> ids) {
        bookRepository.deleteAllById(ids);
        log.info("[ADMIN_BOOKS_BULK_DELETE] Count: {}", ids.size());
    }

    private void updateBookFields(Book book, AdminBookDto dto) {
        book.setPublishYear(dto.getPublishYear());
        book.setPages(dto.getPages());
        book.setCoverImageUrl(dto.getCoverImageUrl());
        book.setStatus(BookStatus.PRELIMINARY);
        book.setPopularityCount(0);

        if (dto.getCategoryId() != null) {
            book.setCategory(categoryRepository.getReferenceById(dto.getCategoryId()));
        }

        if (dto.getAuthorIds() != null) {
            var authors = authorRepository.findAllById(dto.getAuthorIds());
            book.setAuthors(new HashSet<>(authors));
        } else if (book.getAuthors() == null) {
            book.setAuthors(new HashSet<>());
        }

        updateTranslations(book, dto);
    }

    private void updateTranslations(Book book, AdminBookDto dto) {
        if (dto.getTranslations() == null) {
            return;
        }

        if (book.getTranslations() == null) {
            book.setTranslations(new HashMap<>());
        }

        var existing = book.getTranslations();
        dto.getTranslations().forEach((lang, transDto) -> {
            var translation = existing.computeIfAbsent(lang, l -> BookTranslation.builder()
                    .languageCode(l)
                    .book(book)
                    .build());
            translation.setTitle(transDto.getTitle());
            translation.setBookLanguage(transDto.getBookLanguage());
            translation.setDescription(transDto.getDescription());
        });
    }

}

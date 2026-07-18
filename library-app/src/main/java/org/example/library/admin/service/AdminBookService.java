package org.example.library.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.admin.dto.AdminBookDto;
import org.example.library.admin.dto.AdminBookDto.AdminBookTranslationDto;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBookService {

    private static final String BOOK_NOT_FOUND_ERROR_MSG = "error.book.not_found";

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public AdminBookDto getBook(Integer id) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND_ERROR_MSG));
        var bookTranslationsDto = toAdminBookTranslationDto(book.getTranslations());

        return AdminBookDto.builder()
                .id(book.getId())
                .categoryId(book.getCategory() != null
                        ? book.getCategory().getId()
                        : null)
                .publishYear(book.getPublishYear())
                .pages(book.getPages())
                .coverImageUrl(book.getCoverImageUrl())
                .authorIds(book.getAuthors().stream().map(Author::getId).toList())
                .translations(bookTranslationsDto)
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
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND_ERROR_MSG));
        updateBookFields(book, dto);

        bookRepository.save(book);
        log.info("[ADMIN_BOOK_UPDATE] Book ID: {}", id);
    }

    @Transactional
    public void deleteBook(Integer id) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(BOOK_NOT_FOUND_ERROR_MSG));

        bookRepository.delete(book);
        log.info("[ADMIN_BOOK_DELETE] Book ID: {}", id);
    }

    @Transactional
    public void deleteBooks(List<Integer> ids) {
        bookRepository.deleteAllById(ids);
        log.info("[ADMIN_BOOKS_BULK_DELETE] Count: {}", ids.size());
    }

    private Map<String, AdminBookTranslationDto> toAdminBookTranslationDto(Map<String, BookTranslation> translations) {
        return translations.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> toAdminBookTranslationDto(entry.getValue())));
    }

    private AdminBookTranslationDto toAdminBookTranslationDto(BookTranslation bookTranslation) {
        return AdminBookTranslationDto.builder()
                .title(bookTranslation.getTitle())
                .bookLanguage(bookTranslation.getBookLanguage())
                .description(bookTranslation.getDescription())
                .build();
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

        var translations = book.getTranslations();
        dto.getTranslations().forEach((languageCode, translationDto) -> {
            var translation = translations.computeIfAbsent(languageCode, ignored -> new BookTranslation());

            translation.setLanguageCode(languageCode);
            translation.setBook(book);
            translation.setTitle(translationDto.getTitle());
            translation.setBookLanguage(translationDto.getBookLanguage());
            translation.setDescription(translationDto.getDescription());
        });
    }

}

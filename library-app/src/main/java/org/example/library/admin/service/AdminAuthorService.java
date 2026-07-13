package org.example.library.admin.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.library.admin.dto.AdminAuthorDto;
import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.book.repository.BookRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public AdminAuthorDto getAuthor(Integer id) {
        var author = authorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("error.author.not_found"));

        return AdminAuthorDto.builder()
                .id(author.getId())
                .birthYear(author.getBirthYear())
                .deathYear(author.getDeathYear())
                .translations(author.getTranslations().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                e -> AdminAuthorDto.AdminAuthorTranslationDto.builder()
                                        .fullName(e.getValue().getFullName())
                                        .country(e.getValue().getCountry())
                                        .biography(e.getValue().getBiography())
                                        .build())))
                .build();
    }

    @Transactional
    public void createAuthor(AdminAuthorDto dto) {
        var author = new Author();
        updateAuthorFields(author, dto);

        var savedAuthor = authorRepository.save(author);
        log.info("[ADMIN_AUTHOR_CREATE] Author ID: {}", savedAuthor.getId());
    }

    @Transactional
    public void updateAuthor(Integer id, AdminAuthorDto dto) {
        var author = authorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("error.author.not_found"));
        updateAuthorFields(author, dto);

        authorRepository.save(author);
        log.info("[ADMIN_AUTHOR_UPDATE] Author ID: {}", id);
    }

    @Transactional
    public void deleteAuthor(Integer id) {
        var author = authorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("error.author.not_found"));

        if (bookRepository.existsByAuthorsId(id))
            throw new BadRequestException("error.author.has_books");

        authorRepository.delete(author);
        log.info("[ADMIN_AUTHOR_DELETE] Author ID: {}", id);
    }

    @Transactional
    public void deleteAuthors(List<Integer> ids) {
        if (bookRepository.existsByAuthorsIdIn(ids)) {
            throw new BadRequestException("error.author.has_books");
        }

        authorRepository.deleteAllById(ids);
        log.info("[ADMIN_AUTHORS_BULK_DELETE] Count: {}", ids.size());
    }

    private void updateAuthorFields(Author author, AdminAuthorDto dto) {
        author.setBirthYear(dto.getBirthYear());
        author.setDeathYear(dto.getDeathYear());

        updateTranslations(author, dto);
    }

    private void updateTranslations(Author author, AdminAuthorDto dto) {
        if (dto.getTranslations() == null) {
            return;
        }

        var existing = author.getTranslations();
        dto.getTranslations().forEach((languageCode, transDto) -> {
            var translation = existing.computeIfAbsent(languageCode, l -> new AuthorTranslation());

            translation.setLanguageCode(languageCode);
            translation.setAuthor(author);
            translation.setFullName(transDto.getFullName());
            translation.setCountry(transDto.getCountry());
            translation.setBiography(transDto.getBiography());
        });
    }

}

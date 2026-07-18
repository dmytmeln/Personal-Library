package org.example.library.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.admin.dto.AdminAuthorDto;
import org.example.library.admin.dto.AdminAuthorDto.AdminAuthorTranslationDto;
import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.book.repository.BookRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthorService {

    private static final String AUTHOR_NOT_FOUND_ERROR_MSG = "error.author.not_found";

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public AdminAuthorDto getAuthor(Integer id) {
        var author = authorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(AUTHOR_NOT_FOUND_ERROR_MSG));
        var authorTranslationsDto = toAdminAuthorTranslationDto(author.getTranslations());

        return AdminAuthorDto.builder()
                .id(author.getId())
                .birthYear(author.getBirthYear())
                .deathYear(author.getDeathYear())
                .translations(authorTranslationsDto)
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
                .orElseThrow(() -> new NotFoundException(AUTHOR_NOT_FOUND_ERROR_MSG));
        updateAuthorFields(author, dto);

        authorRepository.save(author);
        log.info("[ADMIN_AUTHOR_UPDATE] Author ID: {}", id);
    }

    @Transactional
    public void deleteAuthor(Integer id) {
        var author = authorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(AUTHOR_NOT_FOUND_ERROR_MSG));

        if (bookRepository.existsByAuthorsId(id)) {
            throw new BadRequestException("error.author.has_books");
        }

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

    private Map<String, AdminAuthorTranslationDto> toAdminAuthorTranslationDto(Map<String, AuthorTranslation> translations) {
        return translations.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> toAdminAuthorTranslationDto(entry.getValue())));
    }

    private AdminAuthorTranslationDto toAdminAuthorTranslationDto(AuthorTranslation authorTranslation) {
        return AdminAuthorTranslationDto.builder()
                .fullName(authorTranslation.getFullName())
                .country(authorTranslation.getCountry())
                .biography(authorTranslation.getBiography())
                .build();
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

        var translations = author.getTranslations();
        dto.getTranslations().forEach((languageCode, translationDto) -> {
            var translation = translations.computeIfAbsent(languageCode, ignored -> new AuthorTranslation());

            translation.setLanguageCode(languageCode);
            translation.setAuthor(author);
            translation.setFullName(translationDto.getFullName());
            translation.setCountry(translationDto.getCountry());
            translation.setBiography(translationDto.getBiography());
        });
    }

}

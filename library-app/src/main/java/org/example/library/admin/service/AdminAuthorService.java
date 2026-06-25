package org.example.library.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.admin.dto.AdminAuthorDto;
import org.example.library.author.domain.Author;
import org.example.library.author.domain.AuthorTranslation;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.book.repository.BookRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> AdminAuthorDto.AdminAuthorTranslationDto.builder()
                                .fullName(e.getValue().getFullName())
                                .country(e.getValue().getCountry())
                                .biography(e.getValue().getBiography())
                                .build())))
                .build();
    }

    @Transactional
    public void createAuthor(AdminAuthorDto dto) {
        var author = new Author();
        author.setPopularityCount(0);
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
        if (!authorRepository.existsById(id))
            throw new NotFoundException("error.author.not_found");

        if (bookRepository.existsByAuthorsId(id))
            throw new BadRequestException("error.author.has_books");

        authorRepository.deleteById(id);
        log.info("[ADMIN_AUTHOR_DELETE] Author ID: {}", id);
    }

    @Transactional
    public void deleteAuthors(List<Integer> ids) {
        for (var id : ids) {
            if (bookRepository.existsByAuthorsId(id)) {
                throw new BadRequestException("error.author.has_books");
            }
        }

        authorRepository.deleteAllById(ids);
        log.info("[ADMIN_AUTHORS_BULK_DELETE] Count: {}", ids.size());
    }

    private void updateAuthorFields(Author author, AdminAuthorDto dto) {
        author.setBirthYear(dto.getBirthYear());
        author.setDeathYear(dto.getDeathYear());

        if (dto.getTranslations() != null) {
            if (author.getTranslations() == null) {
                author.setTranslations(new HashMap<>());
            }

            var existingTranslations = author.getTranslations();
            for (var entry : dto.getTranslations().entrySet()) {
                var lang = entry.getKey();
                var transDto = entry.getValue();

                var translation = existingTranslations.get(lang);
                if (translation == null) {
                    translation = AuthorTranslation.builder()
                            .languageCode(lang)
                            .author(author)
                            .build();
                    existingTranslations.put(lang, translation);
                }

                translation.setAuthor(author);
                translation.setFullName(transDto.getFullName());
                translation.setCountry(transDto.getCountry());
                translation.setBiography(transDto.getBiography());
            }
        }
    }

}

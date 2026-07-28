package org.example.library.author.service;

import org.example.library.author.domain.Author;
import org.example.library.author.dto.AuthorResponse;
import org.example.library.author.dto.AuthorSaveRequest;
import org.example.library.author.dto.AuthorSaveRequest.AuthorTranslationRequest;
import org.example.library.author.dto.CountryWithCount;
import org.example.library.author.mapper.AuthorMapper;
import org.example.library.author.repository.AuthorRepository;
import org.example.library.book.repository.BookRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.example.library.common.localization.DefaultLanguage;
import org.example.library.common.pagination.PageRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository repository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorMapper mapper;

    @Mock
    private PageRequestBuilder pageRequestBuilder;

    @Mock
    private DefaultLanguage defaultLanguage;

    @InjectMocks
    private AuthorService authorService;

    @BeforeEach
    void setUp() {
        lenient().when(defaultLanguage.code()).thenReturn("en");
    }

    @Test
    void shouldGetAuthorSuccessfully() {
        var author = Author.builder().id(1).birthYear((short) 1900).build();
        var authorResponse = AuthorResponse.builder().id(1).birthYear((short) 1900).build();

        when(repository.findById(1)).thenReturn(Optional.of(author));
        when(mapper.toAuthorResponse(author)).thenReturn(authorResponse);

        var result = authorService.getAuthorWithAllTranslations(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(repository).findById(1);
    }

    @Test
    void shouldThrowNotFoundWhenGetAuthorDoesNotExist() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.getAuthorWithAllTranslations(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.author.not_found");
    }

    @Test
    void shouldSaveAuthorSuccessfully() {
        var request = AuthorSaveRequest.builder()
                .birthYear((short) 1950)
                .translations(Map.of("en", AuthorTranslationRequest.builder()
                        .fullName("Author Name")
                        .country("USA")
                        .build()))
                .build();
        var savedAuthor = Author.builder().id(10).birthYear((short) 1950).build();
        var authorResponse = AuthorResponse.builder().id(10).birthYear((short) 1950).build();

        when(repository.save(any(Author.class))).thenReturn(savedAuthor);
        when(mapper.toAuthorResponse(savedAuthor)).thenReturn(authorResponse);

        var result = authorService.saveAuthor(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10);
        verify(repository).save(any(Author.class));
    }

    @Test
    void shouldThrowBadRequestWhenSavingWithoutDefaultTranslation() {
        var request = AuthorSaveRequest.builder()
                .birthYear((short) 1950)
                .translations(Map.of("fr", AuthorTranslationRequest.builder()
                        .fullName("Nom")
                        .country("France")
                        .build()))
                .build();

        assertThatThrownBy(() -> authorService.saveAuthor(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.author.default_translation_missing");
    }

    @Test
    void shouldUpdateAuthorSuccessfully() {
        var request = AuthorSaveRequest.builder()
                .birthYear((short) 1950)
                .deathYear((short) 2010)
                .translations(Map.of("en", AuthorTranslationRequest.builder()
                        .fullName("Updated Name")
                        .country("USA")
                        .build()))
                .build();
        var existingAuthor = Author.builder().id(1).birthYear((short) 1950).translations(new HashMap<>()).build();
        var savedAuthor = Author.builder().id(1).birthYear((short) 1950).deathYear((short) 2010).build();
        var authorResponse = AuthorResponse.builder().id(1).birthYear((short) 1950).deathYear((short) 2010).build();

        when(repository.findById(1)).thenReturn(Optional.of(existingAuthor));
        when(repository.save(existingAuthor)).thenReturn(savedAuthor);
        when(mapper.toAuthorResponse(savedAuthor)).thenReturn(authorResponse);

        var result = authorService.updateAuthor(1, request);

        assertThat(result).isNotNull();
        assertThat(result.getDeathYear()).isEqualTo((short) 2010);
        verify(repository).save(existingAuthor);
    }

    @Test
    void shouldDeleteAuthorSuccessfully() {
        var author = Author.builder().id(1).build();

        when(repository.findById(1)).thenReturn(Optional.of(author));
        when(bookRepository.existsByAuthorsId(1)).thenReturn(false);

        authorService.deleteAuthor(1);

        verify(repository).delete(author);
    }

    @Test
    void shouldThrowExceptionWhenDeletingAuthorWithBooks() {
        var author = Author.builder().id(1).build();

        when(repository.findById(1)).thenReturn(Optional.of(author));
        when(bookRepository.existsByAuthorsId(1)).thenReturn(true);

        assertThatThrownBy(() -> authorService.deleteAuthor(1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.author.has_books");
    }

    @Test
    void shouldDeleteAuthorsBulkSuccessfully() {
        when(bookRepository.existsByAuthorsIdIn(List.of(1, 2))).thenReturn(false);

        authorService.deleteAuthors(List.of(1, 2));

        verify(repository).deleteAllByIdInBatch(List.of(1, 2));
    }

    @Test
    void shouldGetAuthorCountriesWithCountSuccessfully() {
        CountryWithCount country = mock(CountryWithCount.class);
        List<CountryWithCount> expected = List.of(country);

        when(repository.findAllAuthorCountriesWithCount(any(), any())).thenReturn(expected);

        var result = authorService.getAuthorCountriesWithCount();

        assertThat(result).isEqualTo(expected);
        verify(repository).findAllAuthorCountriesWithCount(any(), any());
    }

    @Test
    void shouldGetUserAuthorCountriesWithCountSuccessfully() {
        CountryWithCount country = mock(CountryWithCount.class);
        List<CountryWithCount> expected = List.of(country);

        when(repository.findAllAuthorCountriesWithCountForUser(any(), any(), any())).thenReturn(expected);

        var result = authorService.getUserAuthorCountriesWithCount(1);

        assertThat(result).isEqualTo(expected);
        verify(repository).findAllAuthorCountriesWithCountForUser(eq(1), any(), any());
    }

}

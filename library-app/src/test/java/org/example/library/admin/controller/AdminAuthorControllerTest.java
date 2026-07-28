package org.example.library.admin.controller;

import org.example.library.author.dto.AuthorResponse;
import org.example.library.author.dto.AuthorSaveRequest;
import org.example.library.author.service.AuthorService;
import org.example.library.library_book.dto.BulkRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthorControllerTest {

    // todo mock mvc

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private AdminAuthorController adminAuthorController;

    @Test
    void shouldGetAuthor() {
        var response = AuthorResponse.builder().id(1).birthYear((short) 1950).build();
        when(authorService.getAuthorWithAllTranslations(1)).thenReturn(response);

        var result = adminAuthorController.getAuthor(1);

        assertThat(result).isEqualTo(response);
        verify(authorService).getAuthorWithAllTranslations(1);
    }

    @Test
    void shouldCreateAuthor() {
        var request = AuthorSaveRequest.builder().birthYear((short) 1950).build();
        var response = AuthorResponse.builder().id(1).birthYear((short) 1950).build();
        when(authorService.saveAuthor(request)).thenReturn(response);

        var result = adminAuthorController.createAuthor(request);

        assertThat(result).isEqualTo(response);
        verify(authorService).saveAuthor(request);
    }

    @Test
    void shouldUpdateAuthor() {
        var request = AuthorSaveRequest.builder().birthYear((short) 1950).build();
        var response = AuthorResponse.builder().id(1).birthYear((short) 1950).build();
        when(authorService.updateAuthor(1, request)).thenReturn(response);

        var result = adminAuthorController.updateAuthor(1, request);

        assertThat(result).isEqualTo(response);
        verify(authorService).updateAuthor(1, request);
    }

    @Test
    void shouldDeleteAuthor() {
        adminAuthorController.deleteAuthor(1);

        verify(authorService).deleteAuthor(1);
    }

    @Test
    void shouldDeleteAuthorsBulk() {
        var bulkRequest = new BulkRequest();
        bulkRequest.setIds(List.of(1, 2));

        adminAuthorController.deleteAuthors(bulkRequest);

        verify(authorService).deleteAuthors(List.of(1, 2));
    }

}

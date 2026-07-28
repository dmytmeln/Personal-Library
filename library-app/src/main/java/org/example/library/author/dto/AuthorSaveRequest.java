package org.example.library.author.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorSaveRequest {

    @NotNull(message = "{validation.year.required}")
    private Short birthYear;

    private Short deathYear;

    @NotEmpty(message = "{validation.bulk.not_empty}")
    @Valid
    private Map<String, AuthorTranslationRequest> translations;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorTranslationRequest {

        @NotBlank(message = "{validation.full_name.required}")
        private String fullName;

        @NotBlank
        private String country;

        private String biography;

    }

}

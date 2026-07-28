package org.example.library.author.dto;

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
public class AuthorResponse {

    private Integer id;
    private Short birthYear;
    private Short deathYear;
    private Map<String, AuthorTranslationResponse> translations;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorTranslationResponse {

        private String fullName;
        private String country;
        private String biography;

    }

}

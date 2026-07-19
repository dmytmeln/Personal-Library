package org.example.library.admin.dto;

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
public class AdminAuthorDto {

    private Integer id;
    private Short birthYear;
    private Short deathYear;
    private Map<String, AdminAuthorTranslationDto> translations;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminAuthorTranslationDto {
        private String fullName;
        private String country;
        private String biography;
    }

}

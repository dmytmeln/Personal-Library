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
public class AdminBookDto {
    private Integer id;
    private Integer categoryId;
    private Short publishYear;
    private Short pages;
    private String coverImageUrl;
    private Map<String, AdminBookTranslationDto> translations;
    private Iterable<Integer> authorIds;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminBookTranslationDto {
        private String title;
        private String bookLanguage;
        private String description;
    }
}

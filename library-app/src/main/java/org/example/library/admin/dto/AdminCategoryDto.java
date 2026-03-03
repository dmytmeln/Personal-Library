package org.example.library.admin.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCategoryDto {
    private Integer id;
    private Map<String, AdminCategoryTranslationDto> translations;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminCategoryTranslationDto {
        private String name;
        private String description;
    }
}

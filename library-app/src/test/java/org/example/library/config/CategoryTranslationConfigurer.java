package org.example.library.config;

import org.example.library.category.domain.CategoryTranslation;

public class CategoryTranslationConfigurer {

    private final String languageCode;

    private String name = "Test Category";
    private String description = "Default category description";

    public CategoryTranslationConfigurer(String languageCode) {
        this.languageCode = languageCode;
    }

    public CategoryTranslationConfigurer name(String name) {
        this.name = name;
        return this;
    }

    public CategoryTranslationConfigurer description(String description) {
        this.description = description;
        return this;
    }

    public CategoryTranslation build() {
        return CategoryTranslation.builder()
                .languageCode(languageCode)
                .name(name)
                .description(description)
                .build();
    }

}

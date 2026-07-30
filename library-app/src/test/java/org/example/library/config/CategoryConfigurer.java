package org.example.library.config;

import org.example.library.category.domain.Category;

import java.util.HashMap;
import java.util.Map;

public class CategoryConfigurer {

    private final TestDbClient testDbClient;
    private final CategoryTranslationConfigurer defaultTranslation = new CategoryTranslationConfigurer("en");

    private int popularityCount = 0;

    public CategoryConfigurer(TestDbClient testDbClient) {
        this.testDbClient = testDbClient;
    }

    public CategoryConfigurer name(String name) {
        defaultTranslation.name(name);
        return this;
    }

    public CategoryConfigurer description(String description) {
        defaultTranslation.description(description);
        return this;
    }

    public CategoryConfigurer popularityCount(int popularityCount) {
        this.popularityCount = popularityCount;
        return this;
    }

    public Category save() {
        var category = Category.builder()
                .popularityCount(popularityCount)
                .build();

        var enTranslation = defaultTranslation.build();
        enTranslation.setCategory(category);
        category.setTranslations(new HashMap<>(Map.of("en", enTranslation)));

        testDbClient.saveCategory(category);
        return category;
    }

}

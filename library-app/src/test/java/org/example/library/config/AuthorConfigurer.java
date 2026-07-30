package org.example.library.config;

import org.example.library.author.domain.Author;

import java.util.HashMap;
import java.util.Map;

public class AuthorConfigurer {

    private final TestDbClient testDbClient;
    private final AuthorTranslationConfigurer defaultTranslation = new AuthorTranslationConfigurer("en");

    private short birthYear = 1900;
    private Short deathYear;
    private int popularityCount = 0;

    public AuthorConfigurer(TestDbClient testDbClient) {
        this.testDbClient = testDbClient;
    }

    public AuthorConfigurer fullName(String fullName) {
        defaultTranslation.fullName(fullName);
        return this;
    }

    public AuthorConfigurer country(String country) {
        defaultTranslation.country(country);
        return this;
    }

    public AuthorConfigurer biography(String biography) {
        defaultTranslation.biography(biography);
        return this;
    }

    public AuthorConfigurer birthYear(short birthYear) {
        this.birthYear = birthYear;
        return this;
    }

    public AuthorConfigurer deathYear(Short deathYear) {
        this.deathYear = deathYear;
        return this;
    }

    public AuthorConfigurer popularityCount(int popularityCount) {
        this.popularityCount = popularityCount;
        return this;
    }

    public Author save() {
        var author = Author.builder()
                .birthYear(birthYear)
                .deathYear(deathYear)
                .popularityCount(popularityCount)
                .build();

        var enTranslation = defaultTranslation.build();
        enTranslation.setAuthor(author);
        author.setTranslations(new HashMap<>(Map.of("en", enTranslation)));

        testDbClient.saveAuthor(author);
        return author;
    }

}

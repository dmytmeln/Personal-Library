package org.example.library.config;

import org.example.library.author.domain.AuthorTranslation;

public class AuthorTranslationConfigurer {

    private final String languageCode;
    private String fullName = "Test Author";
    private String country = "Unknown";
    private String biography = "Default biography";

    public AuthorTranslationConfigurer(String languageCode) {
        this.languageCode = languageCode;
    }

    public AuthorTranslationConfigurer fullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public AuthorTranslationConfigurer country(String country) {
        this.country = country;
        return this;
    }

    public AuthorTranslationConfigurer biography(String biography) {
        this.biography = biography;
        return this;
    }

    public AuthorTranslation build() {
        return AuthorTranslation.builder()
                .languageCode(languageCode)
                .fullName(fullName)
                .country(country)
                .biography(biography)
                .build();
    }

}

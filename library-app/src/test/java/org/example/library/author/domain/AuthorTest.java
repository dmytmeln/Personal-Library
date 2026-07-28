package org.example.library.author.domain;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorTest {

    private static final String EN_LANG = "en";
    private static final String FR_LANG = "fr";
    private static final String DE_LANG = "de";

    @Test
    void getTranslationOrDefault_ShouldReturnRequestedTranslation_WhenRequestedTranslationExists() {
        var author = createAuthorWithTranslations();

        var actual = author.getTranslationOrDefault(FR_LANG, EN_LANG);

        assertThat(actual).isNotNull();
        assertThat(actual.getLanguageCode()).isEqualTo(FR_LANG);
        assertThat(actual.getFullName()).isEqualTo("Nom Français");
    }

    @Test
    void getTranslationOrDefault_ShouldReturnDefaultTranslation_WhenRequestedTranslationMissing() {
        var author = createAuthorWithTranslations();

        var actual = author.getTranslationOrDefault(DE_LANG, EN_LANG);

        assertThat(actual).isNotNull();
        assertThat(actual.getLanguageCode()).isEqualTo(EN_LANG);
        assertThat(actual.getFullName()).isEqualTo("English Name");
    }

    @Test
    void getTranslationOrDefault_ShouldReturnNull_WhenBothRequestedAndDefaultTranslationsMissing() {
        var author = Author.builder()
                .birthYear((short) 1900)
                .build();
        author.setTranslations(new HashMap<>(Map.of(EN_LANG, buildEnTranslation(author))));

        var result = author.getTranslationOrDefault(DE_LANG, FR_LANG);

        assertThat(result).isNull();
    }

    @Test
    void upsertTranslation_ShouldAddNewTranslation_WhenTranslationDoesNotExist() {
        var author = Author.builder()
                .birthYear((short) 1900)
                .build();

        author.upsertTranslation(EN_LANG, "English Name", "USA", "Bio");

        var actual = author.getTranslation(EN_LANG);
        assertThat(actual).isNotNull();
        assertThat(actual.getLanguageCode()).isEqualTo(EN_LANG);
        assertThat(actual.getFullName()).isEqualTo("English Name");
        assertThat(actual.getCountry()).isEqualTo("USA");
        assertThat(actual.getBiography()).isEqualTo("Bio");
        assertThat(actual.getAuthor()).isEqualTo(author);
    }

    @Test
    void upsertTranslation_ShouldUpdateExistingTranslation_WhenTranslationExists() {
        var author = createAuthorWithTranslations();

        author.upsertTranslation(EN_LANG, "Updated English Name", "UK", "Updated Bio");

        var actual = author.getTranslation(EN_LANG);
        assertThat(actual).isNotNull();
        assertThat(actual.getFullName()).isEqualTo("Updated English Name");
        assertThat(actual.getCountry()).isEqualTo("UK");
        assertThat(actual.getBiography()).isEqualTo("Updated Bio");
    }

    private static Author createAuthorWithTranslations() {
        var author = Author.builder()
                .birthYear((short) 1900)
                .build();

        var enTranslation = buildEnTranslation(author);
        var frTranslation = AuthorTranslation.builder()
                .languageCode(FR_LANG)
                .fullName("Nom Français")
                .country("France")
                .author(author)
                .build();
        author.setTranslations(new HashMap<>(Map.of(EN_LANG, enTranslation, FR_LANG, frTranslation)));

        return author;
    }

    private static AuthorTranslation buildEnTranslation(Author author) {
        return AuthorTranslation.builder()
                .languageCode(EN_LANG)
                .fullName("English Name")
                .country("USA")
                .author(author)
                .build();
    }

}

package org.example.library.config;

import org.example.library.book.domain.BookTranslation;

public class BookTranslationConfigurer {

    private final String languageCode;

    private String title = "Test Book";
    private String bookLanguage = "English";
    private String description = "Default description";

    public BookTranslationConfigurer(String languageCode) {
        this.languageCode = languageCode;
    }

    public BookTranslationConfigurer title(String title) {
        this.title = title;
        return this;
    }

    public BookTranslationConfigurer bookLanguage(String bookLanguage) {
        this.bookLanguage = bookLanguage;
        return this;
    }

    public BookTranslationConfigurer description(String description) {
        this.description = description;
        return this;
    }

    public BookTranslation build() {
        return BookTranslation.builder()
                .languageCode(languageCode)
                .title(title)
                .bookLanguage(bookLanguage)
                .description(description)
                .build();
    }

}

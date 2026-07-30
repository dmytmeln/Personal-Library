package org.example.library.config;

import org.example.library.library_book.domain.LibraryBook;
import org.example.library.quote.domain.Quote;

public class QuoteConfigurer {

    private final TestDbClient testDbClient;

    private LibraryBook libraryBook;
    private boolean libraryBookSet;
    private String text = "Test quote text";
    private String page;
    private String comment;

    public QuoteConfigurer(TestDbClient testDbClient) {
        this.testDbClient = testDbClient;
    }

    public QuoteConfigurer libraryBook(LibraryBook libraryBook) {
        this.libraryBook = libraryBook;
        this.libraryBookSet = true;
        return this;
    }

    public QuoteConfigurer text(String text) {
        this.text = text;
        return this;
    }

    public QuoteConfigurer page(String page) {
        this.page = page;
        return this;
    }

    public QuoteConfigurer comment(String comment) {
        this.comment = comment;
        return this;
    }

    public Quote save() {
        if (!libraryBookSet) {
            libraryBook = new LibraryBookConfigurer(testDbClient).save();
        }

        var quote = Quote.builder()
                .libraryBook(libraryBook)
                .text(text)
                .page(page)
                .comment(comment)
                .build();

        testDbClient.saveQuote(quote);
        return quote;
    }

}

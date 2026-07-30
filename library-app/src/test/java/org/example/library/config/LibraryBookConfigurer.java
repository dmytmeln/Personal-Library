package org.example.library.config;

import org.example.library.book.domain.Book;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.library_book.domain.LibraryBookStatus;
import org.example.library.user.domain.User;

import java.time.LocalDate;

import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;

public class LibraryBookConfigurer {

    private final TestDbClient testDbClient;

    private User user;
    private boolean userSet;
    private Book book;
    private boolean bookSet;
    private LibraryBookStatus status = NO_TAG;
    private String title;
    private Short publishYear;
    private Short pages;
    private String language;
    private String description;
    private String location;
    private Byte rating;
    private LocalDate finishedAt;
    private String customAuthorName;
    private String customCategoryName;

    public LibraryBookConfigurer(TestDbClient testDbClient) {
        this.testDbClient = testDbClient;
    }

    public LibraryBookConfigurer user(User user) {
        this.user = user;
        this.userSet = true;
        return this;
    }

    public LibraryBookConfigurer book(Book book) {
        this.book = book;
        this.bookSet = true;
        return this;
    }

    public LibraryBookConfigurer status(LibraryBookStatus status) {
        this.status = status;
        return this;
    }

    public LibraryBookConfigurer title(String title) {
        this.title = title;
        return this;
    }

    public LibraryBookConfigurer publishYear(Short publishYear) {
        this.publishYear = publishYear;
        return this;
    }

    public LibraryBookConfigurer pages(Short pages) {
        this.pages = pages;
        return this;
    }

    public LibraryBookConfigurer language(String language) {
        this.language = language;
        return this;
    }

    public LibraryBookConfigurer description(String description) {
        this.description = description;
        return this;
    }

    public LibraryBookConfigurer location(String location) {
        this.location = location;
        return this;
    }

    public LibraryBookConfigurer rating(Byte rating) {
        this.rating = rating;
        return this;
    }

    public LibraryBookConfigurer finishedAt(LocalDate finishedAt) {
        this.finishedAt = finishedAt;
        return this;
    }

    public LibraryBookConfigurer customAuthorName(String customAuthorName) {
        this.customAuthorName = customAuthorName;
        return this;
    }

    public LibraryBookConfigurer customCategoryName(String customCategoryName) {
        this.customCategoryName = customCategoryName;
        return this;
    }

    public LibraryBook save() {
        if (!userSet) {
            user = new UserConfigurer(testDbClient).save();
        }
        if (!bookSet) {
            book = new BookConfigurer(testDbClient).save();
        }

        var bookTranslation = book.getTranslation("en");
        var resolvedTitle = title != null ? title : bookTranslation.getTitle();
        var resolvedPublishYear = publishYear != null ? publishYear : book.getPublishYear();
        var resolvedPages = pages != null ? pages : book.getPages();
        var resolvedLanguage = language != null ? language : bookTranslation.getBookLanguage();

        var libraryBook = LibraryBook.builder()
                .user(user)
                .book(book)
                .status(status)
                .title(resolvedTitle)
                .publishYear(resolvedPublishYear)
                .pages(resolvedPages)
                .language(resolvedLanguage)
                .description(description)
                .location(location)
                .rating(rating)
                .finishedAt(finishedAt)
                .customAuthorName(customAuthorName)
                .customCategoryName(customCategoryName)
                .build();

        testDbClient.saveLibraryBook(libraryBook);
        return libraryBook;
    }

}

package org.example.library.config;

import org.example.library.author.domain.Author;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.BookStatus;
import org.example.library.category.domain.Category;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.example.library.book.domain.BookStatus.PRELIMINARY;

public class BookConfigurer {

    private final TestDbClient testDbClient;
    private final BookTranslationConfigurer defaultTranslation = new BookTranslationConfigurer("en");
    private final Set<Author> authors = new HashSet<>();

    private Short publishYear = 2000;
    private Short pages = 100;
    private String coverImageUrl = "default.jpg";
    private BookStatus status = PRELIMINARY;
    private Integer popularityCount = 0;
    private float[] embedding;
    private Category category;
    private boolean categorySet;

    public BookConfigurer(TestDbClient testDbClient) {
        this.testDbClient = testDbClient;
    }

    public BookConfigurer title(String title) {
        defaultTranslation.title(title);
        return this;
    }

    public BookConfigurer bookLanguage(String bookLanguage) {
        defaultTranslation.bookLanguage(bookLanguage);
        return this;
    }

    public BookConfigurer description(String description) {
        defaultTranslation.description(description);
        return this;
    }

    public BookConfigurer publishYear(short publishYear) {
        this.publishYear = publishYear;
        return this;
    }

    public BookConfigurer pages(short pages) {
        this.pages = pages;
        return this;
    }

    public BookConfigurer coverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
        return this;
    }

    public BookConfigurer status(BookStatus status) {
        this.status = status;
        return this;
    }

    public BookConfigurer embedding(float[] embedding) {
        this.embedding = embedding;
        return this;
    }

    public BookConfigurer popularityCount(int popularityCount) {
        this.popularityCount = popularityCount;
        return this;
    }

    public BookConfigurer category(Category category) {
        this.category = category;
        this.categorySet = true;
        return this;
    }

    public BookConfigurer authors(Author... authors) {
        this.authors.addAll(Arrays.asList(authors));
        return this;
    }

    public Book save() {
        if (!categorySet) {
            category = new CategoryConfigurer(testDbClient).save();
        }

        var book = Book.builder()
                .publishYear(publishYear)
                .pages(pages)
                .coverImageUrl(coverImageUrl)
                .status(status)
                .popularityCount(popularityCount)
                .embedding(embedding)
                .category(category)
                .authors(authors)
                .build();

        var enTranslation = defaultTranslation.build();
        enTranslation.setBook(book);
        book.setTranslations(new HashMap<>(Map.of("en", enTranslation)));

        testDbClient.saveBook(book);

        for (var author : authors) {
            testDbClient.linkBookToAuthor(book.getId(), author.getId());
        }

        return book;
    }

}

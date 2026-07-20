package org.example.library.config;

import lombok.NoArgsConstructor;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.example.library.auth.domain.RefreshToken_;
import org.example.library.author.domain.AuthorTranslation_;
import org.example.library.author.domain.Author_;
import org.example.library.book.domain.BookTranslation_;
import org.example.library.book.domain.Book_;
import org.example.library.category.domain.CategoryTranslation_;
import org.example.library.category.domain.Category_;
import org.example.library.collection.domain.Collection_;
import org.example.library.collection_book.domain.CollectionBook_;
import org.example.library.library_book.domain.LibraryBook_;
import org.example.library.note.domain.Note_;
import org.example.library.quote.domain.Quote_;
import org.example.library.reading_goal.domain.ReadingGoal_;
import org.example.library.recommendation.domain.UserProfileVector_;
import org.example.library.user.domain.User_;

import java.time.Duration;
import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class EntityRecursiveComparisonConfigs {

    public static final RecursiveComparisonConfiguration USER_DIRECT_FIELDS = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(User_.REFRESH_TOKENS)
            .build();

    public static final RecursiveComparisonConfiguration USER_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(User_.REFRESH_TOKENS, User_.ID)
            .build();

    public static final RecursiveComparisonConfiguration REFRESH_TOKEN_DIRECT_FIELDS = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(RefreshToken_.USER)
            .withEqualsForType((i1, i2) -> Duration.between(i1, i2).abs().toMillis() <= 1, Instant.class)
            .build();

    public static final RecursiveComparisonConfiguration REFRESH_TOKEN_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(RefreshToken_.USER, RefreshToken_.ID, RefreshToken_.CREATED_AT)
            .withEqualsForType((i1, i2) -> Duration.between(i1, i2).abs().toMillis() <= 1, Instant.class)
            .build();

    public static final RecursiveComparisonConfiguration AUTHOR_DIRECT_FIELDS = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(Author_.BOOKS, Author_.TRANSLATIONS)
            .build();

    public static final RecursiveComparisonConfiguration AUTHOR_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(Author_.BOOKS, Author_.TRANSLATIONS, Author_.ID)
            .build();

    public static final RecursiveComparisonConfiguration AUTHOR_TRANSLATION_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(AuthorTranslation_.AUTHOR, AuthorTranslation_.AUTHOR_ID)
            .build();

    public static final RecursiveComparisonConfiguration CATEGORY_DIRECT_FIELDS = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(Category_.BOOKS, Category_.TRANSLATIONS)
            .build();

    public static final RecursiveComparisonConfiguration CATEGORY_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(Category_.BOOKS, Category_.TRANSLATIONS, Category_.ID)
            .build();

    public static final RecursiveComparisonConfiguration CATEGORY_TRANSLATION_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(CategoryTranslation_.CATEGORY, CategoryTranslation_.CATEGORY_ID)
            .build();

    public static final RecursiveComparisonConfiguration BOOK_DIRECT_FIELDS = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(Book_.OWNER, Book_.CATEGORY, Book_.TRANSLATIONS, Book_.AUTHORS)
            .build();

    public static final RecursiveComparisonConfiguration BOOK_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(Book_.OWNER, Book_.CATEGORY, Book_.TRANSLATIONS, Book_.AUTHORS, Book_.ID)
            .build();

    public static final RecursiveComparisonConfiguration BOOK_TRANSLATION_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(BookTranslation_.BOOK, BookTranslation_.BOOK_ID)
            .build();

    public static final RecursiveComparisonConfiguration COLLECTION_DIRECT_FIELDS = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(Collection_.PARENT, Collection_.USER, Collection_.COLLECTION_BOOKS, Collection_.CHILDREN,
                    Collection_.CREATED_AT, Collection_.UPDATED_AT)
            .build();

    public static final RecursiveComparisonConfiguration COLLECTION_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(Collection_.PARENT, Collection_.USER, Collection_.COLLECTION_BOOKS, Collection_.CHILDREN, Collection_.ID,
                    Collection_.CREATED_AT, Collection_.UPDATED_AT)
            .build();

    public static final RecursiveComparisonConfiguration COLLECTION_BOOK_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(CollectionBook_.COLLECTION, CollectionBook_.LIBRARY_BOOK, CollectionBook_.ADDED_AT, "isNew",
                    "libraryBookView")
            .build();

    public static final RecursiveComparisonConfiguration LIBRARY_BOOK_DIRECT_FIELDS = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(LibraryBook_.BOOK, LibraryBook_.USER, LibraryBook_.ADDED_AT)
            .build();

    public static final RecursiveComparisonConfiguration LIBRARY_BOOK_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(LibraryBook_.BOOK, LibraryBook_.USER, LibraryBook_.ID, LibraryBook_.ADDED_AT)
            .build();

    public static final RecursiveComparisonConfiguration NOTE_DIRECT_FIELDS = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(Note_.LIBRARY_BOOK, Note_.CREATED_AT, Note_.UPDATED_AT)
            .build();

    public static final RecursiveComparisonConfiguration NOTE_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(Note_.LIBRARY_BOOK, Note_.ID, Note_.CREATED_AT, Note_.UPDATED_AT)
            .build();

    public static final RecursiveComparisonConfiguration QUOTE_DIRECT_FIELDS = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(Quote_.LIBRARY_BOOK, Quote_.CREATED_AT, Quote_.UPDATED_AT)
            .build();

    public static final RecursiveComparisonConfiguration QUOTE_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(Quote_.LIBRARY_BOOK, Quote_.ID, Quote_.CREATED_AT, Quote_.UPDATED_AT)
            .build();

    public static final RecursiveComparisonConfiguration READING_GOAL_DIRECT_FIELDS = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(ReadingGoal_.USER)
            .build();

    public static final RecursiveComparisonConfiguration READING_GOAL_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(ReadingGoal_.USER, ReadingGoal_.ID)
            .build();

    public static final RecursiveComparisonConfiguration USER_PROFILE_VECTOR_DIRECT_FIELDS = RecursiveComparisonConfiguration.builder()
            .build();

    public static final RecursiveComparisonConfiguration USER_PROFILE_VECTOR_SAVED = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields(UserProfileVector_.UPDATED_AT)
            .build();

}

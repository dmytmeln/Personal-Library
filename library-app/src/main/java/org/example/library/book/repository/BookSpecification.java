package org.example.library.book.repository;

import jakarta.persistence.criteria.JoinType;
import org.example.library.author.domain.Author_;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.Book_;
import org.example.library.book.dto.BookSearchParams;
import org.example.library.category.domain.Category_;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class BookSpecification {

    public static Specification<Book> fromSearchParams(BookSearchParams searchParams) {
        return Specification.where(hasCategoryId(searchParams.getCategoryId()))
                .and(hasAuthorId(searchParams.getAuthorId()))
                .and(hasTitleLike(searchParams.getTitle()))
                .and(hasPublishYearBetween(searchParams.getPublishYearMin(), searchParams.getPublishYearMax()))
                .and(hasLanguageIn(searchParams.getLanguages()))
                .and(hasPagesBetween(searchParams.getPagesMin(), searchParams.getPagesMax()));
    }

    public static Specification<Book> hasCategoryId(Integer categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null)
                return null;

            return cb.equal(root.get(Book_.CATEGORY).get(Category_.ID), categoryId);
        };
    }

    public static Specification<Book> hasAuthorId(Integer authorId) {
        return (root, query, cb) -> {
            if (authorId == null)
                return null;

            return cb.equal(root.join(Book_.AUTHORS, JoinType.INNER).get(Author_.ID), authorId);
        };
    }

    public static Specification<Book> hasTitleLike(String title) {
        return (root, query, cb) -> {
            if (title == null || title.isBlank())
                return null;

            return cb.like(cb.lower(root.get(Book_.TITLE)), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<Book> hasPublishYearBetween(Short minYear, Short maxYear) {
        return (root, query, cb) -> {
            if (minYear == null && maxYear == null)
                return null;

            if (minYear != null && maxYear != null)
                return cb.between(root.get(Book_.PUBLISH_YEAR), minYear, maxYear);

            if (minYear != null)
                return cb.greaterThanOrEqualTo(root.get(Book_.PUBLISH_YEAR), minYear);

            return cb.lessThanOrEqualTo(root.get(Book_.PUBLISH_YEAR), maxYear);
        };
    }

    public static Specification<Book> hasLanguageIn(List<String> languages) {
        return (root, query, cb) -> {
            if (languages == null || languages.isEmpty())
                return null;

            return root.get(Book_.LANGUAGE).in(languages);
        };
    }

    public static Specification<Book> hasPagesBetween(Short minPages, Short maxPages) {
        return (root, query, cb) -> {
            if (minPages == null && maxPages == null)
                return null;

            if (minPages != null && maxPages != null)
                return cb.between(root.get(Book_.PAGES), minPages, maxPages);

            if (minPages != null)
                return cb.greaterThanOrEqualTo(root.get(Book_.PAGES), minPages);

            return cb.lessThanOrEqualTo(root.get(Book_.PAGES), maxPages);
        };
    }

}

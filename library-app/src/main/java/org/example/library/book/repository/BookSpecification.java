package org.example.library.book.repository;

import jakarta.persistence.criteria.JoinType;
import org.example.library.author.domain.Author_;
import org.example.library.book.domain.Book;
import org.example.library.book.domain.Book_;
import org.example.library.book.dto.BookSearchParams;
import org.example.library.category.domain.Category_;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {

    public static Specification<Book> fromSearchParams(BookSearchParams searchParams) {
        return Specification.where(hasCategoryId(searchParams.getCategoryId()))
                .and(hasAuthorId(searchParams.getAuthorId()));
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

}

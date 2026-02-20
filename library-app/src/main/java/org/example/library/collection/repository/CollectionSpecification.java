package org.example.library.collection.repository;

import jakarta.persistence.criteria.JoinType;
import org.example.library.collection.domain.Collection;
import org.example.library.collection.domain.Collection_;
import org.example.library.collection_book.domain.CollectionBook_;
import org.example.library.library_book.domain.LibraryBook_;
import org.example.library.user.domain.User_;
import org.springframework.data.jpa.domain.Specification;

public class CollectionSpecification {

    public static Specification<Collection> belongsToUser(Integer userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;
            return cb.equal(root.get(Collection_.USER).get(User_.ID), userId);
        };
    }

    public static Specification<Collection> containsLibraryBook(Integer libraryBookId) {
        return (root, query, cb) -> {
            if (libraryBookId == null) return null;
            return cb.equal(root.join(Collection_.COLLECTION_BOOKS, JoinType.INNER)
                    .get(CollectionBook_.LIBRARY_BOOK)
                    .get(LibraryBook_.ID), libraryBookId);
        };
    }

    public static Specification<Collection> withUserIdAndOptionalLibraryBookId(Integer userId, Integer libraryBookId) {
        return Specification.where(belongsToUser(userId))
                .and(containsLibraryBook(libraryBookId));
    }
}

package org.example.library.collection.repository;

import lombok.NoArgsConstructor;
import org.example.library.collection.domain.Collection;
import org.example.library.collection.domain.Collection_;
import org.example.library.collection_book.domain.CollectionBookId_;
import org.example.library.collection_book.domain.CollectionBook_;
import org.example.library.user.domain.User_;
import org.springframework.data.jpa.domain.Specification;

import static jakarta.persistence.criteria.JoinType.INNER;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class CollectionSpecification {

    public static Specification<Collection> belongsToUser(Integer userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return null;
            }

            return cb.equal(root.get(Collection_.USER).get(User_.ID), userId);
        };
    }

    public static Specification<Collection> containsLibraryBook(Integer libraryBookId) {
        return (root, query, cb) -> {
            if (libraryBookId == null) {
                return null;
            }

            return cb.equal(root.join(Collection_.COLLECTION_BOOKS, INNER)
                    .get(CollectionBook_.ID)
                    .get(CollectionBookId_.LIBRARY_BOOK_ID),
                    libraryBookId);
        };
    }

    public static Specification<Collection> withUserIdAndOptionalLibraryBookId(Integer userId, Integer libraryBookId) {
        return Specification.where(belongsToUser(userId)).and(containsLibraryBook(libraryBookId));
    }

}

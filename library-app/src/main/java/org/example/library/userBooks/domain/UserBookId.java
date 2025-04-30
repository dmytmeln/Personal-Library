package org.example.library.userBooks.domain;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record UserBookId(
        Integer userId,
        Integer bookId
) {

    public UserBookId() {
        this(null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserBookId userBookId)) return false;
        return Objects.equals(userId, userBookId.userId()) && Objects.equals(bookId, userBookId.bookId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, bookId);
    }

}

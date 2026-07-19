package org.example.library.quote.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.library.library_book.domain.LibraryBook;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(name = "quotes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quote {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "quotes_seq")
    @SequenceGenerator(name = "quotes_seq", sequenceName = "quotes_seq", allocationSize = 20)
    @Column(name = "quote_id")
    private Integer id;

    @Column(name = "text", nullable = false, columnDefinition = "text")
    private String text;

    @Column(name = "page", length = 50)
    private String page;

    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "library_book_id", nullable = false)
    @OnDelete(action = CASCADE)
    private LibraryBook libraryBook;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Quote quote)) {
            return false;
        }

        return Objects.equals(this.id, quote.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

}

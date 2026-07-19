package org.example.library.reading_goal.domain;

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
import org.example.library.user.domain.User;

import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "reading_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingGoal {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "reading_goals_seq")
    @SequenceGenerator(name = "reading_goals_seq", sequenceName = "reading_goals_seq", allocationSize = 20)
    @Column(name = "goal_id")
    private Integer id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "target_books", nullable = false)
    @Builder.Default
    private Integer targetBooks = 0;

    @Column(name = "target_pages")
    @Builder.Default
    private Integer targetPages = 0;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReadingGoal that)) {
            return false;
        }

        return Objects.equals(this.id, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

}

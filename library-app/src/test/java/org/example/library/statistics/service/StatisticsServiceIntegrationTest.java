package org.example.library.statistics.service;

import org.example.library.config.AbstractServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.example.library.library_book.domain.LibraryBookStatus.READ;
import static org.example.library.library_book.domain.LibraryBookStatus.READING;

class StatisticsServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private StatisticsService service;

    @Test
    void shouldReturnDashboardStats() {
        var user = saveUser(u -> u.email("stats_test@example.com").fullName("Stats Test User").password("pass"));
        var category = saveCategory(c -> c.name("Fiction").description("Description of Fiction"));
        var author = saveAuthor(a -> a.fullName("Author 1").country("UK").birthYear((short) 1970));
        var book = saveBook(b -> b.title("Title").bookLanguage("English").description("Desc").category(category).authors(author).pages((short) 300));
        var lb1 = saveLibraryBook(lb -> lb.user(user).book(book).status(READ).finishedAt(LocalDate.of(2023, 5, 10)).rating((byte) 5).pages((short) 300).language("English"));
        var lb2 = saveLibraryBook(lb -> lb.user(user).book(book).status(READING));
        var lb3 = saveLibraryBook(lb -> lb.user(user).book(book).status(READ).finishedAt(LocalDate.of(2023, 11, 15)).rating((byte) 4));

        var stats = service.getDashboardStats(user.getId(), 2023);

        assertThat(stats).isNotNull();
        var summary = stats.getSummary();
        assertThat(summary.getTotalLibraryBooks()).isEqualTo(3L);
        assertThat(summary.getBooksReadCount()).isEqualTo(2L);
        assertThat(summary.getPagesReadCount()).isEqualTo(600L);
        assertThat(summary.getAverageRating()).isEqualTo(4.5);
        assertThat(summary.getCurrentlyReadingCount()).isEqualTo(1L);
        assertThat(summary.getTotalRatedBooks()).isEqualTo(2L);
        assertThat(stats.getCategoryDistribution()).hasSize(1);
        assertThat(stats.getCategoryDistribution().get(0).getCategoryName()).isEqualTo("Fiction");
        assertThat(stats.getCategoryDistribution().get(0).getCount()).isEqualTo(3L);
        assertThat(stats.getStatusDistribution()).hasSize(2);
        assertThat(stats.getStatusDistribution())
                .filteredOn(s -> s.getStatus() == READ)
                .extracting("count")
                .containsExactly(2L);
        assertThat(stats.getStatusDistribution())
                .filteredOn(s -> s.getStatus() == READING)
                .extracting("count")
                .containsExactly(1L);
        assertThat(stats.getLanguageDistribution()).hasSize(1);
        assertThat(stats.getLanguageDistribution())
                .extracting("language", "count")
                .containsExactly(tuple("English", 3L));
        assertThat(stats.getAuthorCountryDistribution()).hasSize(1);
        assertThat(stats.getAuthorCountryDistribution())
                .extracting("country", "count")
                .containsExactly(tuple("UK", 3L));
        assertThat(stats.getMonthlyReadingActivity()).hasSize(2);
        assertThat(stats.getMonthlyReadingActivity())
                .filteredOn(m -> m.getMonth() == 5)
                .extracting("count")
                .containsExactly(1L);
        assertThat(stats.getMonthlyReadingActivity())
                .filteredOn(m -> m.getMonth() == 11)
                .extracting("count")
                .containsExactly(1L);
        assertThat(stats.getTopAuthors()).hasSize(1);
        assertThat(stats.getTopAuthors())
                .extracting("authorName", "count")
                .containsExactly(tuple("Author 1", 3L));
    }

}

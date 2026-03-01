package org.example.library.statistics.repository;

import org.example.library.library_book.domain.LibraryBook;
import org.example.library.statistics.dto.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface StatisticsRepository extends Repository<LibraryBook, Integer> {

    @Query("""
            SELECT new org.example.library.statistics.dto.DashboardSummaryDto(
                COUNT(lb),
                SUM(CASE WHEN lb.status = 'READ' AND EXTRACT(YEAR FROM lb.finishedAt) = :year THEN 1 ELSE 0 END),
                SUM(CASE WHEN lb.status = 'READ' AND EXTRACT(YEAR FROM lb.finishedAt) = :year THEN COALESCE(lb.pages, b.pages) ELSE 0 END),
                AVG(CASE WHEN lb.status = 'READ' AND EXTRACT(YEAR FROM lb.finishedAt) = :year THEN lb.rating ELSE NULL END),
                SUM(CASE WHEN lb.status = 'READING' THEN 1 ELSE 0 END),
                SUM(CASE WHEN EXTRACT(YEAR FROM lb.addedAt) = :year THEN 1 ELSE 0 END),
                SUM(CASE WHEN lb.rating IS NOT NULL THEN 1 ELSE 0 END)
            )
            FROM LibraryBook lb
            JOIN lb.book b
            WHERE lb.user.id = :userId
            """)
    DashboardSummaryDto getSummary(Integer userId, Integer year);

    @Query("""
            SELECT new org.example.library.statistics.dto.CategoryDistributionDto(
                c.id,
                tr.name,
                COUNT(lb)
            )
            FROM LibraryBook lb
            JOIN lb.book b
            JOIN b.category c
            JOIN c.translations tr ON tr.languageCode = :lang
            WHERE lb.user.id = :userId
            GROUP BY c.id, tr.name
            ORDER BY COUNT(lb) DESC
            """)
    List<CategoryDistributionDto> getCategoryDistribution(Integer userId, String lang);

    @Query("""
            SELECT new org.example.library.statistics.dto.StatusDistributionDto(
                lb.status,
                COUNT(lb)
            )
            FROM LibraryBook lb
            WHERE lb.user.id = :userId
            GROUP BY lb.status
            ORDER BY COUNT(lb) DESC
            """)
    List<StatusDistributionDto> getStatusDistribution(Integer userId);

    @Query("""
            SELECT new org.example.library.statistics.dto.LanguageDistributionDto(
                COALESCE(lb.language, tr.bookLanguage),
                COUNT(lb)
            )
            FROM LibraryBook lb
            JOIN lb.book b
            JOIN b.translations tr ON tr.languageCode = :lang
            WHERE lb.user.id = :userId
            GROUP BY COALESCE(lb.language, tr.bookLanguage)
            ORDER BY COUNT(lb) DESC
            """)
    List<LanguageDistributionDto> getLanguageDistribution(Integer userId, String lang);

    @Query("""
            SELECT new org.example.library.statistics.dto.AuthorCountryDistributionDto(
                tr.country,
                COUNT(lb)
            )
            FROM LibraryBook lb
            JOIN lb.book b
            JOIN b.authors a
            JOIN a.translations tr ON tr.languageCode = :lang
            WHERE lb.user.id = :userId
            GROUP BY tr.country
            ORDER BY COUNT(lb) DESC
            """)
    List<AuthorCountryDistributionDto> getAuthorCountryDistribution(Integer userId, String lang);

    @Query("""
            SELECT new org.example.library.statistics.dto.MonthlyReadingActivityDto(
                CAST(EXTRACT(MONTH FROM lb.finishedAt) AS integer),
                COUNT(lb)
            )
            FROM LibraryBook lb
            WHERE lb.user.id = :userId AND EXTRACT(YEAR FROM lb.finishedAt) = :year AND lb.status = 'READ'
            GROUP BY EXTRACT(MONTH FROM lb.finishedAt)
            ORDER BY EXTRACT(MONTH FROM lb.finishedAt)
            """)
    List<MonthlyReadingActivityDto> getMonthlyReadingActivity(Integer userId, Integer year);

    @Query("""
            SELECT new org.example.library.statistics.dto.TopAuthorDto(
                a.id,
                tr.fullName,
                COUNT(lb)
            )
            FROM LibraryBook lb
            JOIN lb.book b
            JOIN b.authors a
            JOIN a.translations tr ON tr.languageCode = :lang
            WHERE lb.user.id = :userId
            GROUP BY a.id, tr.fullName
            ORDER BY COUNT(lb) DESC
            LIMIT 5
            """)
    List<TopAuthorDto> getTopAuthors(Integer userId, String lang);

}

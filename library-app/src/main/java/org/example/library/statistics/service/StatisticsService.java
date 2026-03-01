package org.example.library.statistics.service;

import lombok.RequiredArgsConstructor;
import org.example.library.statistics.dto.DashboardStatsDto;
import org.example.library.statistics.repository.StatisticsRepository;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository repository;


    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats(Integer userId, Integer year) {
        var lang = LocaleContextHolder.getLocale().getLanguage();

        return DashboardStatsDto.builder()
                .summary(repository.getSummary(userId, year))
                .categoryDistribution(repository.getCategoryDistribution(userId, lang))
                .statusDistribution(repository.getStatusDistribution(userId))
                .languageDistribution(repository.getLanguageDistribution(userId, lang))
                .authorCountryDistribution(repository.getAuthorCountryDistribution(userId, lang))
                .monthlyReadingActivity(repository.getMonthlyReadingActivity(userId, year))
                .topAuthors(repository.getTopAuthors(userId, lang))
                .build();
    }
    
}

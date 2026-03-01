package org.example.library.statistics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardStatsDto {
    private DashboardSummaryDto summary;
    private List<CategoryDistributionDto> categoryDistribution;
    private List<StatusDistributionDto> statusDistribution;
    private List<LanguageDistributionDto> languageDistribution;
    private List<AuthorCountryDistributionDto> authorCountryDistribution;
    private List<MonthlyReadingActivityDto> monthlyReadingActivity;
    private List<TopAuthorDto> topAuthors;
}

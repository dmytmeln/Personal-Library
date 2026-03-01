package org.example.library.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    private Long totalLibraryBooks;
    private Long booksReadCount;
    private Long pagesReadCount;
    private Double averageRating;
    private Long currentlyReadingCount;
    private Long booksAddedThisYear;
    private Long totalRatedBooks;
}

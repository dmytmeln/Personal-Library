package org.example.library.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.library.library_book.domain.LibraryBookStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusDistributionDto {
    private LibraryBookStatus status;
    private Long count;
}

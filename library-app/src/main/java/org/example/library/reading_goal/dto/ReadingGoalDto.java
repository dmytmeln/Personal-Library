package org.example.library.reading_goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingGoalDto {
    private Integer id;
    private Integer year;
    private Integer targetBooks;
    private Integer targetPages;
}

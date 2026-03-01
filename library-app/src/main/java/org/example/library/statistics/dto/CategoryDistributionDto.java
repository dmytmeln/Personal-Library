package org.example.library.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDistributionDto {
    private Integer categoryId;
    private String categoryName;
    private Long count;
}

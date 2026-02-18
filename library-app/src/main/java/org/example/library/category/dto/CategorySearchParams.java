package org.example.library.category.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategorySearchParams {
    private String name;
    private Integer booksCountMin;
    private Integer booksCountMax;
}

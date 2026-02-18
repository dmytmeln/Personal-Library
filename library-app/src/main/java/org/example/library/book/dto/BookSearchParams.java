package org.example.library.book.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BookSearchParams {
    private Integer categoryId;
    private Integer authorId;
    private String title;
    private Short publishYearMin;
    private Short publishYearMax;
    private List<String> languages;
    private Short pagesMin;
    private Short pagesMax;
}

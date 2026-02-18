package org.example.library.author.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthorSearchParams {
    private String name;
    private String country;
    private Short birthYearMin;
    private Short birthYearMax;
    private Integer booksCountMin;
    private Integer booksCountMax;
}

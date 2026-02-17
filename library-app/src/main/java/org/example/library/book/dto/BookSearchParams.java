package org.example.library.book.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookSearchParams {
    private Integer categoryId;
    private Integer authorId;
}

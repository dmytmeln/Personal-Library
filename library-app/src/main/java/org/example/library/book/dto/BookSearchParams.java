package org.example.library.book.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BookSearchParams {
    private Integer categoryId;
    private Integer authorId;
}

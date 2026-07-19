package org.example.library.book.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookTranslationId implements Serializable {
    private Integer bookId;
    private String languageCode;
}

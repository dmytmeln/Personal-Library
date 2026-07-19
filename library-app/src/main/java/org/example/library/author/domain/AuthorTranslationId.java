package org.example.library.author.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorTranslationId implements Serializable {
    private Integer authorId;
    private String languageCode;
}

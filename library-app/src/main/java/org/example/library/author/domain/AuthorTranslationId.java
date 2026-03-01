package org.example.library.author.domain;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorTranslationId implements Serializable {
    private Integer authorId;
    private String languageCode;
}

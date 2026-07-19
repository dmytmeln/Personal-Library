package org.example.library.category.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTranslationId implements Serializable {
    private Integer categoryId;
    private String languageCode;
}

package org.example.library.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {

    private Integer id;
    private String title;
    private Integer categoryId;
    private String categoryName;
    private Short publishYear;
    private String language;
    private Short pages;
    private String description;
    private String coverImageUrl;
    private Map<Integer, String> authors;
    private String customAuthorName;
    private Integer ownerId;

}

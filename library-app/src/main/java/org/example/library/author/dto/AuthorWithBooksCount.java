package org.example.library.author.dto;

public interface AuthorWithBooksCount {
    Integer getId();

    String getFullName();

    String getCountry();

    Short getBirthYear();

    Short getDeathYear();

    Long getBooksCount();
}
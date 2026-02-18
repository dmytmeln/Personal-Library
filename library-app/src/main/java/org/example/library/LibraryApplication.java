package org.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {

    // todo:
    //  BE + FE: add possibility to update full name and email for HOST provided users
    //  BE: dto validation
    //  BE: logging

    // todo:
    //  BE + FE: searching and filtering in search page
    //  FE: rewrite book card to be more compact and to show more info about book
    //  BE + FE: searching and filtering in user library
    //  FE: pagination on author details page
    //  BE + FE: collections
    //  BE + FE: notes
    //  BE + FE: possibility to change book details for library books
    //  BE + FE: statistics
    //  FE: different ui layouts for searching and for library
    //  BE+ FE: recommendations
    //  BE + FE: share library with other users?
    //  FE: use MatSnackBar to show messages to user
    //  BE: JWT refresh tokens
    //  CI/CD
    //  AWS deployment

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

}

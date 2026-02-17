package org.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {

    // todo:
    //  BE + FE: add possibility to update full name and email for HOST provided users
    //  BE: dto validation
    //  BE: logging
    //  BE + FE: searching and filtering in user library
    //  FE: searching and filtering in search page
    //  BE + FE: collections
    //  BE+ FE: recommendations
    //  BE + FE: statistics
    //  BE + FE: notes
    //  BE + FE: possibility to change book details for library books
    //  FE: different layout for book searching and for library
    //  FE: rewrite book card to be more compact and to show more info about book
    //  CI/CD
    //  AWS deployment

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

}

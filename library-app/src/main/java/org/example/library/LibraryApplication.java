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
    //  FE: enhance book card page. Limit title length
    //  FE: fix book details page not to use hardcoded colors but use theme colors from angular material
    //  BE + FE: cant remove library book if it is in collection. Need to remove it from collection first. Maybe add warning about that when trying to remove library book that is in collection?
    //  FE: rewrite library page
    //  BE + FE: searching and filtering in user library
    //  BE + FE: collections
    //  BE + FE: collections pagination, sorting and filtering
    //  BE + FE: notes
    //  BE + FE: possibility to change book details for library books
    //  BE + FE: statistics
    //  FE: different ui layouts for searching and for library
    //  BE+ FE: recommendations
    //  BE + FE: share library with other users?
    //  FE: enhance searching and filtering in search page
    //  FE: use MatSnackBar to show messages to user
    //  BE: JWT refresh tokens
    //  CI/CD
    //  AWS deployment

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

}

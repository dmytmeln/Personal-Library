package org.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {

    // todo:
    //  BE + FE: add possibility to update full name and email for HOST provided users

    // todo:
    //  localization
    //  FE: enhance searching and filtering in search page
    //  FE: grouping layout for library (group by author, genre, etc.)
    //  BE + FE: library - statistics
    //  FE: use MatSnackBar to show messages to user
    //  JPA optimizations + Hibernate bulk configuration
    //  BE+ FE: recommendations
    //  BE + FE: share library with other users?
    //  BE: JWT refresh tokens
    //  BE: password reset
    //  indexes
    //  CI/CD
    //  AWS deployment

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

}

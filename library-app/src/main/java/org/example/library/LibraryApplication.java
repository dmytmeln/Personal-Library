package org.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {

    // todo:
    //  BE + FE: add possibility to update full name and email for HOST provided users

    // todo:
    //  localization
    //  BE + FE: library - statistics
    //  JPA optimizations + Hibernate bulk configuration + change strategy to sequence
    //  BE + FE: recommendations
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

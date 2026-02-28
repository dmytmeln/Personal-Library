package org.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {

    // todo:
    //  localization
    //  BE + FE: library - statistics
    //  BE + FE: share library with other users?
    //  BE: JWT refresh tokens
    //  BE: password reset
    //  confirm email after registration
    //  indexes
    //  BE + FE: recommendations
    //  CI/CD
    //  AWS deployment

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

}

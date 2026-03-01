package org.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {

    // todo:
    //  BE + FE: recommendations, popular books, new arrivals
    //  batch update status of books in library
    //  admin panel
    //  download cover for books
    //  CI/CD
    //  AWS deployment
    //  BE + FE: share library with other users?
    //  BE: JWT refresh tokens
    //  BE: password reset
    //  confirm email after registration

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

}

package org.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {

    // todo:
    //  recalculate books vector category part if category is deleted
    //  save user profile vector and update it when library is updated, instead of calculating it on the fly for every recommendation request
    //  recommendations in author, trends in favorite genres
    //  google books api and openlibrary api integrations for fetching books
    //  admin panel
    //  download cover for books
    //  elk
    //  grafana
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

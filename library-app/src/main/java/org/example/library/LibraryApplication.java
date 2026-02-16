package org.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {

    // todo:
    //  add possibility to update full name and email for HOST provided users
    //  dto validation
    //  logging
    //  pagination and sorting for books
    //  searching and filtering in user library
    //  searching and filtering in search page
    //  collections
    //  recommendation
    //  statistics
    //  notes
    //  ci/cd
    //  aws deployment

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

}

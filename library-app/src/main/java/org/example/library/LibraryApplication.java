package org.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {

    // todo:
    //  create profile page with possibility to update
    //  add data initialization liquibase script
    //  dto validation
    //  logging

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

}

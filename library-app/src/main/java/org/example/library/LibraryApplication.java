package org.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {

    // todo:
    //  jwt + OAuth -> add email (unique), fullName and password (bcrypted) to User -> update migration
    //  frontend needs a route for /oauth2/redirect
    //  create login and auth pages, endpoints, dtos and services
    //  create profile page with possibility to update
    //  implement frontend logout
    //  add data initialization liquibase script
    //  dto validation
    //  logging

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

}

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
    //  BE + FE: delete collection
    //  BE + FE: collection - nested collections
    //  BE + FE: collections - add the possibility to move book from one collection to another (remove from one collection and add to another)
    //  BE + FE: collection - add the possibility to add book to collection from library page (without going to collection page). Maybe dialog with list of collections to choose from?
    //  BE + FE: collection - collections pagination, sorting and filtering
    //  BE + FE: library - searching and filtering in user library
    //  BE + FE: library - possibility to change book details for library books
    //  BE + FE: library - notes
    //  BE + FE: library - statistics
    //  FE: different ui layouts for searching and for library
    //  FE: grouping layout for library (group by author, genre, etc.)
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

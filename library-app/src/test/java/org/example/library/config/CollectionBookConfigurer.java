package org.example.library.config;

import org.example.library.collection.domain.Collection;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.example.library.library_book.domain.LibraryBook;

public class CollectionBookConfigurer {

    private final TestDbClient testDbClient;

    private Collection collection;
    private boolean collectionSet;
    private LibraryBook libraryBook;
    private boolean libraryBookSet;

    public CollectionBookConfigurer(TestDbClient testDbClient) {
        this.testDbClient = testDbClient;
    }

    public CollectionBookConfigurer collection(Collection collection) {
        this.collection = collection;
        this.collectionSet = true;
        return this;
    }

    public CollectionBookConfigurer libraryBook(LibraryBook libraryBook) {
        this.libraryBook = libraryBook;
        this.libraryBookSet = true;
        return this;
    }

    public CollectionBook save() {
        if (!collectionSet) {
            collection = new CollectionConfigurer(testDbClient).save();
        }
        if (!libraryBookSet) {
            libraryBook = new LibraryBookConfigurer(testDbClient).save();
        }

        var id = new CollectionBookId(collection.getId(), libraryBook.getId());
        var collectionBook = CollectionBook.builder()
                .id(id)
                .collection(collection)
                .libraryBook(libraryBook)
                .build();

        testDbClient.saveCollectionBook(collectionBook);
        return collectionBook;
    }

}

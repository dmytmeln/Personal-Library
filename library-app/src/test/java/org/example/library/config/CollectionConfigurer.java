package org.example.library.config;

import org.example.library.collection.domain.Collection;
import org.example.library.user.domain.User;

public class CollectionConfigurer {

    private final TestDbClient testDbClient;

    private User user;
    private boolean userSet;
    private String name = "Test Collection";
    private String description;
    private Collection parent;

    public CollectionConfigurer(TestDbClient testDbClient) {
        this.testDbClient = testDbClient;
    }

    public CollectionConfigurer user(User user) {
        this.user = user;
        this.userSet = true;
        return this;
    }

    public CollectionConfigurer name(String name) {
        this.name = name;
        return this;
    }

    public CollectionConfigurer description(String description) {
        this.description = description;
        return this;
    }

    public CollectionConfigurer parent(Collection parent) {
        this.parent = parent;
        return this;
    }

    public Collection save() {
        if (!userSet) {
            user = new UserConfigurer(testDbClient).save();
        }

        var collection = Collection.builder()
                .user(user)
                .name(name)
                .description(description)
                .parent(parent)
                .build();

        testDbClient.saveCollection(collection);
        return collection;
    }

}

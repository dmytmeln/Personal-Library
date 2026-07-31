package org.example.library.collection.repository;

import org.example.library.book.domain.Book;
import org.example.library.collection.domain.Collection;
import org.example.library.collection.dto.CollectionTreeProjection;
import org.example.library.collection.dto.CollectionValidationProjection;
import org.example.library.collection_book.domain.CollectionBook;
import org.example.library.collection_book.domain.CollectionBookId;
import org.example.library.config.AbstractRepositoryTest;
import org.hibernate.Hibernate;
import org.example.library.library_book.domain.LibraryBook;
import org.example.library.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.library.book.domain.BookStatus.NEW;
import static org.example.library.config.EntityRecursiveComparisonConfigs.COLLECTION_BOOK_SAVED;
import static org.example.library.config.EntityRecursiveComparisonConfigs.COLLECTION_DIRECT_FIELDS;
import static org.example.library.config.EntityRecursiveComparisonConfigs.COLLECTION_SAVED;
import static org.example.library.config.EntityRecursiveComparisonConfigs.USER_DIRECT_FIELDS;
import static org.example.library.library_book.domain.LibraryBookStatus.NO_TAG;
import static org.example.library.user.domain.Role.USER;

class CollectionRepositoryTest extends AbstractRepositoryTest<CollectionRepository> {

    @Test
    void save_ShouldPersistCollection_AndNotCascadeUser() {
        User owner = createUser();
        testDbClient.saveUser(owner);
        long initialUserCount = testDbClient.countUsers();
        Collection expected = createCollection(owner, null);

        Collection actual = repository.save(expected);

        assertThat(actual)
                .usingRecursiveComparison(COLLECTION_SAVED)
                .isEqualTo(expected);
        Collection dbState = testDbClient.findCollectionById(actual.getId());
        assertThat(dbState)
                .isNotNull()
                .usingRecursiveComparison(COLLECTION_DIRECT_FIELDS)
                .isEqualTo(actual);
        assertThat(testDbClient.countUsers()).isEqualTo(initialUserCount);
    }

    @Test
    void save_ShouldPersistCollection_AndCascadeCollectionBooks() {
        User owner = createUser();
        testDbClient.saveUser(owner);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, owner);
        testDbClient.saveLibraryBook(libraryBook);
        Collection expected = createCollection(owner, null);

        Collection actual = transactionTemplate.execute(status -> {
            LibraryBook libraryBookRef = entityManager.getReference(LibraryBook.class, libraryBook.getId());
            CollectionBook collectionBook = CollectionBook.builder()
                    .id(new CollectionBookId(null, libraryBook.getId()))
                    .libraryBook(libraryBookRef)
                    .collection(expected)
                    .build();
            expected.getCollectionBooks().add(collectionBook);

            return repository.saveAndFlush(expected);
        });

        assertThat(actual)
                .usingRecursiveComparison(COLLECTION_SAVED)
                .isEqualTo(expected);
        assertThat(testDbClient.countCollectionBooks()).isEqualTo(1);
        assertThat(testDbClient.countLibraryBooks()).isEqualTo(1L);
        CollectionBook dbCollectionBook = testDbClient.findCollectionBookById(actual.getId(), libraryBook.getId());
        assertThat(dbCollectionBook).isNotNull();
        assertThat(dbCollectionBook.getId().getCollectionId()).isEqualTo(actual.getId());
        assertThat(dbCollectionBook.getId().getLibraryBookId()).isEqualTo(libraryBook.getId());
    }

    @Test
    @Transactional
    void findById_ShouldReturnCollection_WhenCollectionExists() {
        User owner = createUser();
        testDbClient.saveUser(owner);
        Collection parent = createCollection(owner, "Parent");
        testDbClient.saveCollection(parent);
        Collection collection = createCollection(owner, "Main");
        collection.setParent(parent);
        testDbClient.saveCollection(collection);
        Collection child = createCollection(owner, "Child");
        child.setParent(collection);
        testDbClient.saveCollection(child);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, owner);
        testDbClient.saveLibraryBook(libraryBook);
        CollectionBook collectionBook = CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook.getId()))
                .build();
        testDbClient.saveCollectionBook(collectionBook);

        Optional<Collection> actual = repository.findById(collection.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(COLLECTION_DIRECT_FIELDS)
                .isEqualTo(collection);
        assertThat(actual.get().getUser())
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(USER_DIRECT_FIELDS)
                .isEqualTo(owner);
        assertThat(actual.get().getParent())
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(COLLECTION_DIRECT_FIELDS)
                .isEqualTo(parent);
        assertThat(actual.get().getChildren()).hasSize(1);
        assertThat(actual.get().getChildren().get(0))
                .extracting(Hibernate::unproxy)
                .usingRecursiveComparison(COLLECTION_DIRECT_FIELDS)
                .isEqualTo(child);
        assertThat(actual.get().getCollectionBooks()).hasSize(1);
        assertThat(actual.get().getCollectionBooks().get(0))
                .usingRecursiveComparison(COLLECTION_BOOK_SAVED)
                .isEqualTo(collectionBook);
    }

    @Test
    void delete_ShouldRemoveCollection_ByUserScope_AndCascadeCollectionBooks() {
        User owner = createUser();
        testDbClient.saveUser(owner);
        Collection collection = createCollection(owner, null);
        testDbClient.saveCollection(collection);
        Book book = createBook();
        testDbClient.saveBook(book);
        LibraryBook libraryBook = createLibraryBook(book, owner);
        testDbClient.saveLibraryBook(libraryBook);
        testDbClient.saveCollectionBook(CollectionBook.builder()
                .id(new CollectionBookId(collection.getId(), libraryBook.getId()))
                .build());

        transactionTemplate.executeWithoutResult(status -> repository.deleteById(collection.getId(), owner.getId()));

        assertThat(testDbClient.findCollectionById(collection.getId())).isNull();
        assertThat(testDbClient.countCollectionBooks()).isZero();
    }

    @Test
    void findCollectionTreeProjectionsByUserId_ShouldReturnIdNameAndParentId() {
        User owner = createUser();
        testDbClient.saveUser(owner);
        Collection root = createCollection(owner, "Root");
        testDbClient.saveCollection(root);
        Collection child = createCollection(owner, "Child");
        child.setParent(root);
        child.setUser(owner);
        testDbClient.saveCollection(child);

        List<CollectionTreeProjection> actual = repository.findCollectionTreeProjectionsByUserId(owner.getId());

        assertThat(actual).hasSize(2);
        assertThat(actual).extracting(CollectionTreeProjection::name).containsExactlyInAnyOrder("Root", "Child");
        assertThat(actual).filteredOn(projection -> projection.name().equals("Root"))
                .singleElement()
                .satisfies(projection -> {
                    assertThat(projection.id()).isEqualTo(root.getId());
                    assertThat(projection.parentId()).isNull();
                });
        assertThat(actual).filteredOn(projection -> projection.name().equals("Child"))
                .singleElement()
                .satisfies(projection -> {
                    assertThat(projection.id()).isEqualTo(child.getId());
                    assertThat(projection.parentId()).isEqualTo(root.getId());
                });
    }

    @Test
    void findAncestors_ShouldReturnAncestorsInOrder() {
        User owner = createUser();
        testDbClient.saveUser(owner);
        Collection root = createCollection(owner, "Root");
        testDbClient.saveCollection(root);
        Collection child = createCollection(owner, "Child");
        child.setParent(root);
        child.setUser(owner);
        testDbClient.saveCollection(child);
        Collection grandchild = createCollection(owner, "Grandchild");
        grandchild.setParent(child);
        grandchild.setUser(owner);
        testDbClient.saveCollection(grandchild);

        List<Collection> actual = repository.findAncestors(grandchild.getId());

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getName()).isEqualTo("Root");
        assertThat(actual.get(1).getName()).isEqualTo("Child");
    }

    @Test
    void getValidationData_ShouldReturnValidationData() {
        User owner = createUser();
        testDbClient.saveUser(owner);
        Collection root = createCollection(owner, "Root");
        testDbClient.saveCollection(root);
        Collection parent = createCollection(owner, "Parent");
        parent.setParent(root);
        parent.setUser(owner);
        testDbClient.saveCollection(parent);
        Collection toMove = createCollection(owner, "ToMove");
        toMove.setParent(parent);
        toMove.setUser(owner);
        testDbClient.saveCollection(toMove);
        Collection grandchild = createCollection(owner, "Grandchild");
        grandchild.setParent(toMove);
        grandchild.setUser(owner);
        testDbClient.saveCollection(grandchild);

        CollectionValidationProjection actual = repository.getValidationData(toMove.getId(), parent.getId());

        assertThat(actual).isNotNull();
        assertThat(actual.getMovedDescendantLevels()).isEqualTo(2);
        assertThat(actual.getNewParentLevel()).isEqualTo(2);
        assertThat(actual.isCircular()).isFalse();
    }

    @Test
    void getValidationData_ShouldDetectCircularReference() {
        User owner = createUser();
        testDbClient.saveUser(owner);
        Collection root = createCollection(owner, "Root");
        testDbClient.saveCollection(root);
        Collection parent = createCollection(owner, "Parent");
        parent.setParent(root);
        parent.setUser(owner);
        testDbClient.saveCollection(parent);
        Collection toMove = createCollection(owner, "ToMove");
        toMove.setParent(parent);
        toMove.setUser(owner);
        testDbClient.saveCollection(toMove);

        CollectionValidationProjection actual = repository.getValidationData(parent.getId(), toMove.getId());

        assertThat(actual).isNotNull();
        assertThat(actual.isCircular()).isTrue();
    }

    @Test
    void getHierarchyLevel_ShouldReturnHierarchyLevel() {
        User owner = createUser();
        testDbClient.saveUser(owner);
        Collection root = createCollection(owner, "Root");
        testDbClient.saveCollection(root);
        Collection child = createCollection(owner, "Child");
        child.setParent(root);
        child.setUser(owner);
        testDbClient.saveCollection(child);
        Collection grandchild = createCollection(owner, "Grandchild");
        grandchild.setParent(child);
        grandchild.setUser(owner);
        testDbClient.saveCollection(grandchild);

        int rootLevel = repository.getHierarchyLevel(root.getId());
        int childLevel = repository.getHierarchyLevel(child.getId());
        int grandchildLevel = repository.getHierarchyLevel(grandchild.getId());

        assertThat(rootLevel).isEqualTo(1);
        assertThat(childLevel).isEqualTo(2);
        assertThat(grandchildLevel).isEqualTo(3);
    }

    @Test
    void findByIdAndUserIdWithChildren_ShouldReturnCollectionWithChildren() {
        User owner = createUser();
        testDbClient.saveUser(owner);
        Collection parent = createCollection(owner, "Parent");
        testDbClient.saveCollection(parent);
        Collection child1 = createCollection(owner, "Child1");
        child1.setParent(parent);
        child1.setUser(owner);
        testDbClient.saveCollection(child1);
        Collection child2 = createCollection(owner, "Child2");
        child2.setParent(parent);
        child2.setUser(owner);
        testDbClient.saveCollection(child2);

        Optional<Collection> actual = repository.findByIdAndUserIdWithChildren(parent.getId(), owner.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getChildren()).hasSize(2);
    }

    @Test
    void findByIdAndUserId_ShouldReturnCollection_WhenCollectionExists() {
        User owner = createUser();
        testDbClient.saveUser(owner);
        Collection collection = createCollection(owner, "My Collection");
        testDbClient.saveCollection(collection);

        Optional<Collection> actual = repository.findByIdAndUserId(collection.getId(), owner.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison(COLLECTION_DIRECT_FIELDS)
                .isEqualTo(collection);
    }

    @Test
    void findByIdAndUserId_ShouldReturnEmpty_WhenCollectionDoesNotExist() {
        User owner = createUser();
        testDbClient.saveUser(owner);

        Optional<Collection> actual = repository.findByIdAndUserId(999, owner.getId());

        assertThat(actual).isEmpty();
    }

    @Test
    void existsByIdAndUserId_ShouldReturnTrue_WhenCollectionExists() {
        User owner = createUser();
        testDbClient.saveUser(owner);
        Collection collection = createCollection(owner, "My Collection");
        testDbClient.saveCollection(collection);

        boolean actual = repository.existsByIdAndUserId(collection.getId(), owner.getId());

        assertThat(actual).isTrue();
    }

    @Test
    void existsByIdAndUserId_ShouldReturnFalse_WhenCollectionDoesNotExist() {
        User owner = createUser();
        testDbClient.saveUser(owner);

        boolean actual = repository.existsByIdAndUserId(999, owner.getId());

        assertThat(actual).isFalse();
    }

    private User createUser() {
        return User.builder()
                .email("collection-owner@example.com")
                .fullName("Collection Owner")
                .password("pass")
                .role(USER)
                .build();
    }

    private Collection createCollection(User owner, String name) {
        return Collection.builder()
                .name(name != null ? name : "Default Collection")
                .user(owner)
                .build();
    }

    private Book createBook() {
        return Book.builder()
                .publishYear((short) 2020)
                .pages((short) 300)
                .coverImageUrl("http://example.com/cover.png")
                .status(NEW)
                .popularityCount(0)
                .build();
    }

    private LibraryBook createLibraryBook(Book book, User user) {
        return LibraryBook.builder()
                .book(book)
                .user(user)
                .status(NO_TAG)
                .build();
    }

}

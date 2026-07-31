package org.example.library.collection.service;

import org.example.library.collection.dto.CreateCollectionRequest;
import org.example.library.collection.dto.UpdateCollectionDto;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.example.library.config.AbstractServiceIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private CollectionService service;

    @Test
    void shouldReturnAllCollectionsForUser() {
        var user = saveUser();
        saveCollection(c -> c.user(user).name("Collection 1"));
        saveCollection(c -> c.user(user).name("Collection 2"));

        var result = service.getCollectionsContainingLibraryBook(user.getId(), null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("Collection 1", "Collection 2");
    }

    @Test
    void shouldReturnCollectionsContainingLibraryBook() {
        var user = saveUser();
        var collection = saveCollection(c -> c.user(user).name("My Collection"));
        var book = saveBook();
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
        saveCollectionBook(cb -> cb.collection(collection).libraryBook(libraryBook));

        var result = service.getCollectionsContainingLibraryBook(user.getId(), libraryBook.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("My Collection");
    }

    @Test
    void shouldReturnUserCollectionHierarchy() {
        var user = saveUser();
        var root = saveCollection(c -> c.user(user).name("Root"));
        var child = saveCollection(c -> c.user(user).name("Child").parent(root));

        var result = service.getUserCollectionHierarchy(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Root");
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getName()).isEqualTo("Child");
    }

    @Test
    void shouldReturnHierarchySortedAlphabeticallyAtEachLevel() {
        var user = saveUser();
        var zulu = saveCollection(c -> c.user(user).name("Zulu"));
        var alpha = saveCollection(c -> c.user(user).name("Alpha"));
        var mike = saveCollection(c -> c.user(user).name("Mike"));
        var beta = saveCollection(c -> c.user(user).name("Beta").parent(alpha));
        var gamma = saveCollection(c -> c.user(user).name("Gamma").parent(alpha));
        var apple = saveCollection(c -> c.user(user).name("Apple").parent(beta));

        var result = service.getUserCollectionHierarchy(user.getId());

        assertThat(result).extracting("name").containsExactly("Alpha", "Mike", "Zulu");
        assertThat(result.get(0).getChildren()).extracting("name").containsExactly("Beta", "Gamma");
        assertThat(result.get(0).getChildren().get(0).getChildren()).extracting("name").containsExactly("Apple");
    }

    @Test
    void shouldReturnCollectionDetailsWithAncestors() {
        var user = saveUser();
        var root = saveCollection(c -> c.user(user).name("Root"));
        var child = saveCollection(c -> c.user(user).name("Child").parent(root));

        var result = service.getCollectionDetails(child.getId(), user.getId());

        assertThat(result.getName()).isEqualTo("Child");
        assertThat(result.getAncestors()).hasSize(1);
        assertThat(result.getAncestors().get(0).getName()).isEqualTo("Root");
    }

    @Test
    void shouldThrowNotFoundWhenGettingNonExistentCollectionDetails() {
        var user = saveUser();

        assertThatThrownBy(() -> service.getCollectionDetails(-1, user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.collection.not_found");
    }

    @Test
    void shouldCreateCollection() {
        var user = saveUser();
        var request = new CreateCollectionRequest();
        request.setName("New Collection");
        request.setDescription("Description");

        var result = service.createCollection(request, user.getId());

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("New Collection");
        assertThat(testDbClient.findCollectionById(result.getId())).isNotNull();
    }

    @Test
    void shouldCreateSubCollection() {
        var user = saveUser();
        var parent = saveCollection(c -> c.user(user).name("Parent"));
        var request = new CreateCollectionRequest();
        request.setName("Sub");
        request.setParentId(parent.getId());

        var result = service.createCollection(request, user.getId());

        assertThat(result.getName()).isEqualTo("Sub");
        var saved = testDbClient.findCollectionById(result.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getParent().getId()).isEqualTo(parent.getId());
    }

    @Test
    void shouldCreateSubCollectionAtExactMaxHierarchyLevel() {
        var user = saveUser();
        var c1 = saveCollection(c -> c.user(user).name("c1"));
        var c2 = saveCollection(c -> c.user(user).name("c2").parent(c1));
        var c3 = saveCollection(c -> c.user(user).name("c3").parent(c2));

        var request = new CreateCollectionRequest();
        request.setName("c4");
        request.setParentId(c3.getId());

        var result = service.createCollection(request, user.getId());

        assertThat(result.getName()).isEqualTo("c4");
        var saved = testDbClient.findCollectionById(result.getId());
        assertThat(saved.getParent().getId()).isEqualTo(c3.getId());
    }

    @Test
    void shouldThrowBadRequestWhenCreatingCollectionExceedingMaxHierarchyLevel() {
        var user = saveUser();
        var c1 = saveCollection(c -> c.user(user).name("c1"));
        var c2 = saveCollection(c -> c.user(user).name("c2").parent(c1));
        var c3 = saveCollection(c -> c.user(user).name("c3").parent(c2));
        var c4 = saveCollection(c -> c.user(user).name("c4").parent(c3));

        var request = new CreateCollectionRequest();
        request.setName("c5");
        request.setParentId(c4.getId());

        assertThatThrownBy(() -> service.createCollection(request, user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.collection.max_hierarchy_level_exceeded");
    }

    @Test
    void shouldUpdateCollection() {
        var user = saveUser();
        var collection = saveCollection(c -> c.user(user).name("Old Name"));
        var dto = new UpdateCollectionDto();
        dto.setName("New Name");

        var result = service.updateCollection(collection.getId(), dto, user.getId());

        assertThat(result.getName()).isEqualTo("New Name");
        var saved = testDbClient.findCollectionById(collection.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Name");
    }

    @Test
    void shouldMoveCollection() {
        var user = saveUser();
        var parent1 = saveCollection(c -> c.user(user).name("Parent 1"));
        var parent2 = saveCollection(c -> c.user(user).name("Parent 2"));
        var child = saveCollection(c -> c.user(user).name("Child").parent(parent1));

        service.moveCollection(child.getId(), parent2.getId(), user.getId());

        var saved = testDbClient.findCollectionById(child.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getParent().getId()).isEqualTo(parent2.getId());
    }

    @Test
    void shouldMoveCollectionWithinMaxHierarchyLevel() {
        var user = saveUser();
        var c1 = saveCollection(c -> c.user(user).name("c1"));
        var c2 = saveCollection(c -> c.user(user).name("c2").parent(c1));
        var c3 = saveCollection(c -> c.user(user).name("c3").parent(c2));
        var toMove = saveCollection(c -> c.user(user).name("toMove"));

        service.moveCollection(toMove.getId(), c3.getId(), user.getId());

        var saved = testDbClient.findCollectionById(toMove.getId());
        assertThat(saved.getParent().getId()).isEqualTo(c3.getId());
    }

    @Test
    void shouldThrowBadRequestWhenMoveExceedsMaxHierarchyLevel() {
        var user = saveUser();
        var c1 = saveCollection(c -> c.user(user).name("c1"));
        var c2 = saveCollection(c -> c.user(user).name("c2").parent(c1));
        var c3 = saveCollection(c -> c.user(user).name("c3").parent(c2));
        var c4 = saveCollection(c -> c.user(user).name("c4").parent(c3));
        var toMove = saveCollection(c -> c.user(user).name("toMove"));

        assertThatThrownBy(() -> service.moveCollection(toMove.getId(), c4.getId(), user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.collection.max_hierarchy_level_exceeded");
    }

    @Test
    void shouldMakeCollectionRootWhenMovingToNullParent() {
        var user = saveUser();
        var parent = saveCollection(c -> c.user(user).name("Parent"));
        var child = saveCollection(c -> c.user(user).name("Child").parent(parent));

        service.moveCollection(child.getId(), null, user.getId());

        var saved = testDbClient.findCollectionById(child.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getParent()).isNull();
    }

    @Test
    void shouldThrowBadRequestWhenMovingCollectionToItself() {
        var user = saveUser();
        var collection = saveCollection(c -> c.user(user).name("Collection"));

        assertThatThrownBy(() -> service.moveCollection(collection.getId(), collection.getId(), user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.collection.cannot_be_own_parent");
    }

    @Test
    void shouldThrowBadRequestWhenMovingCollectionUnderItsDescendant() {
        var user = saveUser();
        var parent = saveCollection(c -> c.user(user).name("Parent"));
        var child = saveCollection(c -> c.user(user).name("Child").parent(parent));
        var grandchild = saveCollection(c -> c.user(user).name("Grandchild").parent(child));

        assertThatThrownBy(() -> service.moveCollection(parent.getId(), grandchild.getId(), user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.collection.circular_dependency");
    }

    @Test
    void shouldDeleteCollection() {
        var user = saveUser();
        var collection = saveCollection(c -> c.user(user).name("To Delete"));

        service.deleteCollection(collection.getId(), user.getId());

        assertThat(testDbClient.findCollectionById(collection.getId())).isNull();
    }

    @Test
    void shouldMoveBookBetweenCollections() {
        var user = saveUser();
        var source = saveCollection(c -> c.user(user).name("Source"));
        var target = saveCollection(c -> c.user(user).name("Target"));
        var book = saveBook();
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
        saveCollectionBook(cb -> cb.collection(source).libraryBook(libraryBook));

        service.moveBook(source.getId(), target.getId(), libraryBook.getId(), user.getId());

        assertThat(testDbClient.findCollectionBookById(source.getId(), libraryBook.getId())).isNull();
        assertThat(testDbClient.findCollectionBookById(target.getId(), libraryBook.getId())).isNotNull();
    }

    @Test
    void shouldDoNothingWhenMovingBookWithinSameCollection() {
        var user = saveUser();
        var source = saveCollection(c -> c.user(user).name("Source"));
        var book = saveBook();
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
        saveCollectionBook(cb -> cb.collection(source).libraryBook(libraryBook));

        service.moveBook(source.getId(), source.getId(), libraryBook.getId(), user.getId());

        assertThat(testDbClient.findCollectionBookById(source.getId(), libraryBook.getId())).isNotNull();
    }

    @Test
    void shouldThrowNotFoundWhenMovingBookUserDoesNotOwn() {
        var user = saveUser();
        var otherUser = saveUser(u -> u.email("other@example.com"));
        var source = saveCollection(c -> c.user(user).name("Source"));
        var target = saveCollection(c -> c.user(user).name("Target"));
        var book = saveBook();
        var otherLibraryBook = saveLibraryBook(lb -> lb.user(otherUser).book(book));

        assertThatThrownBy(() -> service.moveBook(source.getId(), target.getId(), otherLibraryBook.getId(), user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.library_book.not_found");
    }

    @Test
    void shouldThrowNotFoundWhenMovingBookFromCollectionNotOwned() {
        var user = saveUser();
        var otherUser = saveUser(u -> u.email("other@example.com"));
        var source = saveCollection(c -> c.user(otherUser).name("Source"));
        var target = saveCollection(c -> c.user(user).name("Target"));
        var book = saveBook();
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));

        assertThatThrownBy(() -> service.moveBook(source.getId(), target.getId(), libraryBook.getId(), user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.collection.not_found");
    }

    @Test
    void shouldThrowNotFoundWhenBookNotInSourceCollection() {
        var user = saveUser();
        var source = saveCollection(c -> c.user(user).name("Source"));
        var target = saveCollection(c -> c.user(user).name("Target"));
        var book = saveBook();
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));

        assertThatThrownBy(() -> service.moveBook(source.getId(), target.getId(), libraryBook.getId(), user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.collection.book_not_in_source");
    }

    @Test
    void shouldThrowBadRequestWhenBookAlreadyInTargetCollection() {
        var user = saveUser();
        var source = saveCollection(c -> c.user(user).name("Source"));
        var target = saveCollection(c -> c.user(user).name("Target"));
        var book = saveBook();
        var libraryBook = saveLibraryBook(lb -> lb.user(user).book(book));
        saveCollectionBook(cb -> cb.collection(source).libraryBook(libraryBook));
        saveCollectionBook(cb -> cb.collection(target).libraryBook(libraryBook));

        assertThatThrownBy(() -> service.moveBook(source.getId(), target.getId(), libraryBook.getId(), user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("error.collection.book_already_in_target");
    }

}

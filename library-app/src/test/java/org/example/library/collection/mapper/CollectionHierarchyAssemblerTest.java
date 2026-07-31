package org.example.library.collection.mapper;

import org.example.library.collection.dto.CollectionNodeDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionHierarchyAssemblerTest {

    private final CollectionHierarchyAssembler assembler = new CollectionHierarchyAssembler();

    private static CollectionNodeDto node(Integer id, String name, Integer parentId) {
        return CollectionNodeDto.builder()
                .id(id)
                .name(name)
                .parentId(parentId)
                .build();
    }

    @Test
    void shouldReturnEmptyListForEmptyInput() {
        assertThat(assembler.assemble(List.of())).isEmpty();
    }

    @Test
    void shouldReturnSingleTopLevelNode() {
        var result = assembler.assemble(List.of(node(1, "Root", null)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Root");
        assertThat(result.get(0).getChildren()).isEmpty();
    }

    @Test
    void shouldBuildNestedHierarchyFromFlatList() {
        var root = node(1, "Root", null);
        var child = node(2, "Child", 1);
        var grandchild = node(3, "Grandchild", 2);

        var result = assembler.assemble(List.of(root, child, grandchild));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Root");
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getName()).isEqualTo("Child");
        assertThat(result.get(0).getChildren().get(0).getChildren()).extracting("name").containsExactly("Grandchild");
    }

    @Test
    void shouldReturnMultipleTopLevelNodes() {
        var first = node(1, "First", null);
        var second = node(2, "Second", null);
        var secondChild = node(3, "Child", 2);

        var result = assembler.assemble(List.of(first, second, secondChild));

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("First", "Second");
        assertThat(result.get(1).getChildren()).extracting("name").containsExactly("Child");
    }

    @Test
    void shouldSortByNameCaseInsensitivelyAtEachLevel() {
        var alpha = node(1, "Alpha", null);
        var zulu = node(2, "Zulu", null);
        var mike = node(3, "Mike", null);
        var beta = node(4, "Beta", 1);
        var gamma = node(5, "Gamma", 1);
        var apple = node(6, "Apple", 4);

        var result = assembler.assemble(List.of(zulu, gamma, beta, apple, mike, alpha));

        assertThat(result).extracting("name").containsExactly("Alpha", "Mike", "Zulu");
        assertThat(result.get(0).getChildren()).extracting("name").containsExactly("Beta", "Gamma");
        assertThat(result.get(0).getChildren().get(0).getChildren()).extracting("name").containsExactly("Apple");
    }

    @Test
    void shouldSortCaseInsensitivelyIgnoringCaseOfNames() {
        var lowercase = node(1, "apple", null);
        var uppercase = node(2, "Beta", null);
        var mixedCase = node(3, "cherry", null);

        var result = assembler.assemble(List.of(uppercase, mixedCase, lowercase));

        assertThat(result).extracting("name").containsExactly("apple", "Beta", "cherry");
    }

    @Test
    void shouldThrowNullPointerExceptionForDanglingParentReference() {
        var orphan = node(1, "Orphan", 99);
        var orphans = List.of(orphan);

        assertThatThrownBy(() -> assembler.assemble(orphans))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Collection 1 references parent 99 which is not fetched");
    }

}

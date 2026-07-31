package org.example.library.collection.mapper;

import org.example.library.collection.dto.CollectionNodeDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public final class CollectionHierarchyAssembler {

    private static final Comparator<CollectionNodeDto> NODE_NAME_ORDER = Comparator
            .comparing(CollectionNodeDto::getName, CASE_INSENSITIVE_ORDER)
            .thenComparing(CollectionNodeDto::getId);

    public List<CollectionNodeDto> assemble(List<CollectionNodeDto> nodes) {
        var nodeByCollectionId = nodes.stream()
                .collect(toMap(CollectionNodeDto::getId, identity()));

        var topLevelNodes = new ArrayList<CollectionNodeDto>();
        for (var node : nodes) {
            var parentId = node.getParentId();
            var isTopLevel = parentId == null;
            if (isTopLevel) {
                topLevelNodes.add(node);
                continue;
            }

            var parentNode = requireNonNull(nodeByCollectionId.get(parentId),
                    () -> format("Collection %s references parent %s which is not fetched", node.getId(), parentId));
            parentNode.getChildren().add(node);
        }

        sortHierarchy(topLevelNodes);
        return topLevelNodes;
    }

    private void sortHierarchy(List<CollectionNodeDto> nodes) {
        nodes.sort(NODE_NAME_ORDER);
        for (var node : nodes) {
            var children = node.getChildren();
            if (children.isEmpty()) {
                continue;
            }

            sortHierarchy(children);
        }
    }

}

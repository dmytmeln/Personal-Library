package org.example.library.collection.dto;

public interface CollectionValidationProjection {
    Integer getSubtreeDepth();

    Integer getParentRootDepth();

    Boolean getIsCircular();
}

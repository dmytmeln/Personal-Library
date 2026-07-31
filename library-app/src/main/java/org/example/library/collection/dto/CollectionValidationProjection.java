package org.example.library.collection.dto;

public interface CollectionValidationProjection {

    Integer getNewParentLevel();

    Integer getMovedDescendantLevels();

    boolean isCircular();

}

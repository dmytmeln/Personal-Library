package org.example.library.reading_goal.mapper;

import org.example.library.reading_goal.domain.ReadingGoal;
import org.example.library.reading_goal.dto.ReadingGoalDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReadingGoalMapper {

    ReadingGoalDto toDto(ReadingGoal readingGoal);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "year", ignore = true)
    void update(@MappingTarget ReadingGoal readingGoal, ReadingGoalDto dto);

}

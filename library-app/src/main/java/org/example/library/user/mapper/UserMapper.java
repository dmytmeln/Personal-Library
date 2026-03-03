package org.example.library.user.mapper;

import org.example.library.auth.dto.UserRegisterRequest;
import org.example.library.user.domain.User;
import org.example.library.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "password", ignore = true)
    User toUser(UserRegisterRequest request);

    UserResponse toResponse(User user);

}

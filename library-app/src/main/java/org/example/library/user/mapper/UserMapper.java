package org.example.library.user.mapper;

import org.example.library.auth.dto.UserRegisterRequest;
import org.example.library.user.domain.Provider;
import org.example.library.user.domain.Role;
import org.example.library.user.domain.User;
import org.example.library.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "provider", constant = "HOST")
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "providerId", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toHostUser(UserRegisterRequest request);

    UserResponse toResponse(User user);

    default User createProvidedUser(String email, String fullName, String providerId, Provider provider) {
        var user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setProviderId(providerId);
        user.setProvider(provider);
        user.setRole(Role.USER);
        return user;
    }

}

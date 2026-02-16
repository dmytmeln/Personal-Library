package org.example.library.user.dto;

import lombok.Builder;
import lombok.Data;
import org.example.library.user.domain.Provider;
import org.example.library.user.domain.Role;

@Data
@Builder
public class UserResponse {

    private Integer id;
    private String email;
    private String fullName;
    private Role role;
    private Provider provider;

}

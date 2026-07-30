package org.example.library.config;

import org.example.library.user.domain.Role;
import org.example.library.user.domain.User;

import static org.example.library.user.domain.Role.USER;

public class UserConfigurer {

    private final TestDbClient testDbClient;

    private String email = "test@example.com";
    private String fullName = "Test User";
    private String password = "password";
    private Role role = USER;

    public UserConfigurer(TestDbClient testDbClient) {
        this.testDbClient = testDbClient;
    }

    public UserConfigurer email(String email) {
        this.email = email;
        return this;
    }

    public UserConfigurer fullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public UserConfigurer password(String password) {
        this.password = password;
        return this;
    }

    public UserConfigurer role(Role role) {
        this.role = role;
        return this;
    }

    public User save() {
        var user = User.builder()
                .email(email)
                .fullName(fullName)
                .password(password)
                .role(role)
                .build();

        testDbClient.saveUser(user);
        return user;
    }

}

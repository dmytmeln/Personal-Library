package org.example.library.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.security.UserDetailsImpl;
import org.example.library.user.dto.UserResponse;
import org.example.library.user.mapper.UserMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserMapper userMapper;

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userMapper.toResponse(userDetails.user());
    }

}

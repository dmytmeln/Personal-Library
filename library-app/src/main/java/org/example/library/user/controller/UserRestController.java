package org.example.library.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.library.security.UserDetailsImpl;
import org.example.library.security.jwt.JwtService;
import org.example.library.security.util.CookieUtils;
import org.example.library.user.dto.UpdateProfileRequest;
import org.example.library.user.dto.UserResponse;
import org.example.library.user.mapper.UserMapper;
import org.example.library.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserMapper userMapper;
    private final UserService userService;
    private final JwtService jwtService;

    @Value("${application.security.jwt.cookie-name}")
    private String cookieName;

    @Value("${application.security.jwt.access-token-expiration}")
    private int cookieMaxAge;

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userMapper.toResponse(userDetails.user());
    }

    @PatchMapping("/me")
    public UserResponse updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @Valid @RequestBody UpdateProfileRequest request,
                                      HttpServletResponse response) {
        var oldEmail = userDetails.getUsername();
        var userResponse = userService.updateProfile(userDetails.getId(), request);

        if (!oldEmail.equalsIgnoreCase(userResponse.getEmail())) {
            var newToken = jwtService.generateToken(userResponse.getEmail());
            CookieUtils.addCookie(response, cookieName, newToken, cookieMaxAge);
        }

        return userResponse;
    }

}

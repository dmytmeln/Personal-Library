package org.example.library.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.library.auth.dto.AuthenticationRequest;
import org.example.library.auth.dto.UserRegisterRequest;
import org.example.library.auth.service.AuthService;
import org.example.library.user.dto.UserResponse;
import org.example.library.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final AuthService service;
    private final UserService userService;

    @Value("${application.security.jwt.cookie-name}")
    private String cookieName;

    @Value("${application.security.jwt.access-token-expiration}")
    private int cookieMaxAge;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody @Valid UserRegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/authenticate")
    @ResponseStatus(HttpStatus.OK)
    public void authenticate(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
        var authResponse = service.authenticate(request);
        
        Cookie cookie = new Cookie(cookieName, authResponse.token());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(cookieMaxAge / 1000);
        
        response.addCookie(cookie);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        
        response.addCookie(cookie);
    }

}

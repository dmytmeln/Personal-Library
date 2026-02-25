package org.example.library.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.auth.dto.AuthenticationRequest;
import org.example.library.auth.dto.AuthenticationResponse;
import org.example.library.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public AuthenticationResponse authenticate(AuthenticationRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                ));
        String jwtToken = jwtService.generateToken(authRequest.getEmail());
        log.info("[LOGIN_SUCCESS] User: {}", authRequest.getEmail());
        return new AuthenticationResponse(jwtToken);
    }

}

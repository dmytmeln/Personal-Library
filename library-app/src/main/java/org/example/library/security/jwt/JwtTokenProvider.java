package org.example.library.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements AuthenticationProvider {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var jwtAuth = (JwtTokenAuthentication) authentication;
        if (!jwtService.isTokenValid(jwtAuth.getToken()))
            throw new BadCredentialsException("Invalid JWT token");

        UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.extractUsername(jwtAuth.getToken()));
        return new JwtTokenAuthentication(jwtAuth.getToken(), userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtTokenAuthentication.class.isAssignableFrom(authentication);
    }

}
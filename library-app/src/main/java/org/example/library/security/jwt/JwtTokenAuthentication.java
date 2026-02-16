package org.example.library.security.jwt;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JwtTokenAuthentication implements Authentication {

    @Getter
    private final String token;
    private UserDetails userDetails;
    private boolean authenticated;

    public JwtTokenAuthentication(String token) {
        this.token = token;
    }

    public JwtTokenAuthentication(String token, UserDetails userDetails) {
        this.token = token;
        this.userDetails = userDetails;
        this.authenticated = true;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Optional.ofNullable(userDetails)
                .map(UserDetails::getAuthorities)
                .orElseGet(List::of);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userDetails;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Not supported, use constructor");
        }
    }

    @Override
    public String getName() {
        return Optional.ofNullable(userDetails)
                .map(UserDetails::getUsername)
                .orElse(null);
    }

}
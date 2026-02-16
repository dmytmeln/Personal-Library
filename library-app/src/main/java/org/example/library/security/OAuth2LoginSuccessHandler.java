package org.example.library.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.library.security.jwt.JwtService;
import org.example.library.user.domain.Provider;
import org.example.library.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${application.security.jwt.cookie-name}")
    private String cookieName;

    @Value("${application.security.jwt.access-token-expiration}")
    private int cookieMaxAge;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        var oidcUser = (DefaultOidcUser) authentication.getPrincipal();
        var email = oidcUser.getEmail();
        var fullName = oidcUser.getFullName();
        var providerId = oidcUser.getSubject();

        var user = userService.syncUser(email, fullName, providerId, Provider.GOOGLE);

        var token = jwtService.generateToken(user.getEmail());

        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(cookieMaxAge / 1000);
        
        response.addCookie(cookie);

        this.getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/oauth2/redirect");
    }

}

package org.example.library.user.service;

import lombok.RequiredArgsConstructor;
import org.example.library.user.domain.User;
import org.example.library.user.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OAuth2UserService<OidcUserRequest, OidcUser> delegate = new OidcUserService();
    private final UserRepository repository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);
        return processOidcUser(oidcUser, userRequest);
    }

    private OidcUser processOidcUser(OidcUser oidcUser, OidcUserRequest userRequest) {
        var provider = userRequest.getClientRegistration().getRegistrationId();
        var providerId = oidcUser.getName();
        String fullName = oidcUser.getAttribute("name");
        String pictureUrl = oidcUser.getAttribute("picture");
        var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        var user = repository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> create(provider, providerId));

        return user.toBuilder()
                .attributes(oidcUser.getAttributes())
                .authorities(authorities)
                .fullName(fullName)
                .pictureUrl(pictureUrl)
                .idToken(userRequest.getIdToken())
                .userInfo(oidcUser.getUserInfo())
                .build();
    }

    private User create(String provider, String providerId) {
        var user = User.builder()
                .provider(provider)
                .providerId(providerId)
                .build();
        return repository.save(user);
    }

}

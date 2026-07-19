package org.example.library.common.config;

import org.example.library.common.localization.DefaultLanguage;
import org.example.library.common.pagination.PaginationProperties;
import org.example.library.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
@EnableConfigurationProperties(value = PaginationProperties.class)
public class AppConfig {

    private static final String MESSAGE_BASENAME = "messages";

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService) {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(JwtTokenProvider jwtTokenProvider,
                                                       DaoAuthenticationProvider daoAuthenticationProvider,
                                                       AuthenticationEventPublisher eventPublisher) {
        var providerManager = new ProviderManager(List.of(jwtTokenProvider, daoAuthenticationProvider));
        providerManager.setAuthenticationEventPublisher(eventPublisher);

        return providerManager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DefaultLanguage defaultLanguage(@Value("${application.localization.default-language:en}") String code) {
        return new DefaultLanguage(code);
    }

    @Bean
    public LocaleResolver localeResolver(DefaultLanguage defaultLanguage) {
        var slr = new AcceptHeaderLocaleResolver();
        slr.setDefaultLocale(Locale.forLanguageTag(defaultLanguage.code()));

        return slr;
    }

    @Bean
    public MessageSource messageSource() {
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename(MESSAGE_BASENAME);
        messageSource.setDefaultEncoding(UTF_8.name());
        messageSource.setFallbackToSystemLocale(false);

        return messageSource;
    }

}

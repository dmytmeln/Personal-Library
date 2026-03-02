package org.example.library.recommendations.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.book.dto.BookDto;
import org.example.library.recommendations.service.RecommendationService;
import org.example.library.security.UserDetailsImpl;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationsController {

    private final RecommendationService recommendationService;

    @GetMapping
    public List<BookDto> getRecommendations(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @RequestParam(required = false) Integer limit) {
        var languageCode = LocaleContextHolder.getLocale().getLanguage();
        return recommendationService.getRecommendations(userDetails.getId(), languageCode, limit);
    }

    @GetMapping("/popular")
    public List<BookDto> getPopularBooks(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @RequestParam(required = false) Integer limit) {
        var languageCode = LocaleContextHolder.getLocale().getLanguage();
        return recommendationService.getPopularBooks(userDetails.getId(), languageCode, limit);
    }

    @GetMapping("/new")
    public List<BookDto> getNewArrivals(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestParam(required = false) Integer limit) {
        var languageCode = LocaleContextHolder.getLocale().getLanguage();
        return recommendationService.getNewArrivals(userDetails.getId(), languageCode, limit);
    }

}

package org.example.library.recommendations.service;

import lombok.RequiredArgsConstructor;
import org.example.library.book.dto.BookDto;
import org.example.library.book.mapper.BookMapper;
import org.example.library.book.repository.BookDisplayViewRepository;
import org.example.library.exception.InvalidPaginationParameterException;
import org.example.library.pagination.PaginationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserProfileService userProfileService;
    private final BookDisplayViewRepository bookDisplayViewRepository;
    private final BookMapper bookMapper;
    private final PaginationProperties paginationProperties;


    @Transactional(readOnly = true)
    public List<BookDto> getRecommendations(Integer userId, String languageCode, Integer limit) {
        float[] userVector = userProfileService.calculateUserProfileVector(userId);

        if (userVector == null)
            return Collections.emptyList();

        int validatedLimit = validateLimit(limit);
        var similarBooks = bookDisplayViewRepository.findSimilarBooks(userVector, languageCode, userId, validatedLimit);
        return bookMapper.toBookDtos(similarBooks);
    }

    @Transactional(readOnly = true)
    public List<BookDto> getPopularBooks(Integer userId, String languageCode, Integer limit) {
        int validatedLimit = validateLimit(limit);
        var since = LocalDateTime.now().minusMonths(1);
        var books = bookDisplayViewRepository.findPopularBooksRecently(languageCode, userId, since, validatedLimit);
        return bookMapper.toBookDtos(books);
    }

    @Transactional(readOnly = true)
    public List<BookDto> getNewArrivals(Integer userId, String languageCode, Integer limit) {

        int validatedLimit = validateLimit(limit);
        short currentYear = (short) Year.now().getValue();
        var books = bookDisplayViewRepository.findNewArrivals(languageCode, userId, currentYear, validatedLimit);
        return bookMapper.toBookDtos(books);
    }

    private int validateLimit(Integer limit) {
        if (limit == null)
            return paginationProperties.getDefaultPageSize();

        if (limit < paginationProperties.getMinPageSize()) {
            throw new InvalidPaginationParameterException(
                    "error.pagination.min_page_size", paginationProperties.getMinPageSize()
            );
        }

        if (limit > paginationProperties.getMaxPageSize()) {
            throw new InvalidPaginationParameterException(
                    "error.pagination.max_page_size", paginationProperties.getMaxPageSize()
            );
        }

        return limit;
    }

}

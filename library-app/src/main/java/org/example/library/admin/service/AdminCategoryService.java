package org.example.library.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.admin.dto.AdminCategoryDto;
import org.example.library.admin.dto.AdminCategoryDto.AdminCategoryTranslationDto;
import org.example.library.book.repository.BookRepository;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.category.repository.CategoryRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryService {

    private static final String CATEGORY_NOT_FOUND_ERROR_MSG = "error.category.not_found";

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public AdminCategoryDto getCategory(Integer id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_ERROR_MSG));
        var categoryTranslationsDto = toAdminCategoryTranslationDto(category.getTranslations());

        return AdminCategoryDto.builder()
                .id(category.getId())
                .translations(categoryTranslationsDto)
                .build();
    }

    @Transactional
    public void createCategory(AdminCategoryDto dto) {
        var category = new Category();
        category.setPopularityCount(0);
        updateCategoryFields(category, dto);

        var savedCategory = categoryRepository.save(category);
        log.info("[ADMIN_CATEGORY_CREATE] Category ID: {}", savedCategory.getId());
    }

    @Transactional
    public void updateCategory(Integer id, AdminCategoryDto dto) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_ERROR_MSG));
        updateCategoryFields(category, dto);

        categoryRepository.save(category);
        log.info("[ADMIN_CATEGORY_UPDATE] Category ID: {}", id);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_ERROR_MSG));

        if (bookRepository.existsByCategoryId(id)) {
            throw new BadRequestException("error.category.has_books");
        }

        categoryRepository.delete(category);
        log.info("[ADMIN_CATEGORY_DELETE] Category ID: {}", id);
    }

    @Transactional
    public void deleteCategories(List<Integer> ids) {
        if (bookRepository.existsByCategoryIdIn(ids)) {
            throw new BadRequestException("error.category.has_books");
        }

        categoryRepository.deleteAllById(ids);
        log.info("[ADMIN_CATEGORIES_BULK_DELETE] Count: {}", ids.size());
    }

    private Map<String, AdminCategoryTranslationDto> toAdminCategoryTranslationDto(Map<String, CategoryTranslation> translations) {
        return translations.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> toAdminCategoryTranslationDto(entry.getValue())));
    }

    private AdminCategoryTranslationDto toAdminCategoryTranslationDto(CategoryTranslation categoryTranslation) {
        return AdminCategoryTranslationDto.builder()
                .name(categoryTranslation.getName())
                .description(categoryTranslation.getDescription())
                .build();
    }

    private void updateCategoryFields(Category category, AdminCategoryDto dto) {
        updateTranslations(category, dto);
    }

    private void updateTranslations(Category category, AdminCategoryDto dto) {
        if (dto.getTranslations() == null) {
            return;
        }

        var translations = category.getTranslations();
        dto.getTranslations().forEach((languageCode, translationDto) -> {
            var translation = translations.computeIfAbsent(languageCode, ignored -> new CategoryTranslation());

            translation.setLanguageCode(languageCode);
            translation.setCategory(category);
            translation.setName(translationDto.getName());
            translation.setDescription(translationDto.getDescription());
        });
    }

}

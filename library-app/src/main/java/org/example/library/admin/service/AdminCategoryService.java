package org.example.library.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.library.admin.dto.AdminCategoryDto;
import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryTranslation;
import org.example.library.category.repository.CategoryRepository;
import org.example.library.book.repository.BookRepository;
import org.example.library.common.exception.BadRequestException;
import org.example.library.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public AdminCategoryDto getCategory(Integer id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("error.category.not_found"));

        return AdminCategoryDto.builder()
                .id(category.getId())
                .translations(category.getTranslations().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> AdminCategoryDto.AdminCategoryTranslationDto.builder()
                                .name(e.getValue().getName())
                                .description(e.getValue().getDescription())
                                .build())))
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
                .orElseThrow(() -> new NotFoundException("error.category.not_found"));
        updateCategoryFields(category, dto);

        categoryRepository.save(category);
        log.info("[ADMIN_CATEGORY_UPDATE] Category ID: {}", id);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id))
            throw new NotFoundException("error.category.not_found");

        if (bookRepository.existsByCategoryId(id))
            throw new BadRequestException("error.category.has_books");

        categoryRepository.deleteById(id);
        log.info("[ADMIN_CATEGORY_DELETE] Category ID: {}", id);
    }

    @Transactional
    public void deleteCategories(List<Integer> ids) {
        for (var id : ids) {
            if (bookRepository.existsByCategoryId(id)) {
                throw new BadRequestException("error.category.has_books");
            }
        }

        categoryRepository.deleteAllById(ids);
        log.info("[ADMIN_CATEGORIES_BULK_DELETE] Count: {}", ids.size());
    }

    private void updateCategoryFields(Category category, AdminCategoryDto dto) {
        if (dto.getTranslations() != null) {
            if (category.getTranslations() == null) {
                category.setTranslations(new HashMap<>());
            }

            var existingTranslations = category.getTranslations();
            for (var entry : dto.getTranslations().entrySet()) {
                var lang = entry.getKey();
                var transDto = entry.getValue();

                var translation = existingTranslations.get(lang);
                if (translation == null) {
                    translation = CategoryTranslation.builder()
                            .languageCode(lang)
                            .category(category)
                            .build();
                    existingTranslations.put(lang, translation);
                }

                translation.setCategory(category);
                translation.setName(transDto.getName());
                translation.setDescription(transDto.getDescription());
            }
        }
    }

}

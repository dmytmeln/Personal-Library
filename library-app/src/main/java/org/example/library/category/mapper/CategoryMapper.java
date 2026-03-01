package org.example.library.category.mapper;

import org.example.library.category.domain.Category;
import org.example.library.category.domain.CategoryDisplayView;
import org.example.library.category.dto.CategoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "name", source = "category", qualifiedByName = "getLocalizedName")
    @Mapping(target = "description", source = "category", qualifiedByName = "getLocalizedDescription")
    CategoryDto toDto(Category category);

    CategoryDto toDto(CategoryDisplayView categoryDisplayView);

    List<CategoryDto> toDtoList(List<Category> categories);

    @Named("getLocalizedName")
    default String getLocalizedName(Category category) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        var translation = category.getTranslations().get(lang);
        if (translation == null)
            throw new IllegalStateException("Translation not found for category: " + category.getId());

        return translation.getName();
    }

    @Named("getLocalizedDescription")
    default String getLocalizedDescription(Category category) {
        var lang = LocaleContextHolder.getLocale().getLanguage();
        var translation = category.getTranslations().get(lang);
        if (translation == null)
            throw new IllegalStateException("Translation not found for category: " + category.getId());

        return translation.getDescription();
    }

}

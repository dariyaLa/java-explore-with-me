package ru.practicum.categories;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryMapper {

    public static Category toCategory(CategoryDto newDto) {
        return Category.builder()
                .name(newDto.getName())
                .build();
    }

    public static CategoryDtoOut toCategoryDtoOut(Category category) {
        return CategoryDtoOut.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .name(category.getName())
                .build();
    }

}

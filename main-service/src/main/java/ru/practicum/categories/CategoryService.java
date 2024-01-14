package ru.practicum.categories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import ru.practicum.ServiceMain;
import ru.practicum.events.EventRepoImpl;
import ru.practicum.exception.ConflictException;

import java.util.Collection;
import java.util.stream.Collectors;

import static ru.practicum.constants.Constant.CATEGORY_DUPLICATE_EXCEPTION;
import static ru.practicum.constants.Constant.CATEGORY_WITH_EVENTS_EXCEPTION;

@Service
@RequiredArgsConstructor
public class CategoryService implements ServiceMain<CategoryDto, CategoryDtoOut> {

    private final CategoryRepoImpl repository;
    private final EventRepoImpl eventRepo;

    @Override
    public CategoryDtoOut add(CategoryDto categoryDto) {
        Category category = CategoryMapper.toCategory(categoryDto);
        try {
            category = repository.add(category);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(String.format(CATEGORY_DUPLICATE_EXCEPTION, categoryDto.getName()));
        }
        return CategoryMapper.toCategoryDtoOut(category);
    }

    @Override
    public CategoryDtoOut update(CategoryDto categoryDto, Long id) {
        Category category = CategoryMapper.toCategory(categoryDto);
        try {
            category = repository.update(category, id);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(String.format(CATEGORY_DUPLICATE_EXCEPTION, categoryDto.getName()));
        }
        return CategoryMapper.toCategoryDtoOut(category);
    }

    @Override
    public CategoryDtoOut find(Long id) {
        return CategoryMapper.toCategoryDtoOut(repository.find(id));
    }

    @Override
    public Collection<CategoryDtoOut> findAll(Integer from, Integer size) {
        Collection<Category> categories = repository.findAll(from, size);
        return categories.stream()
                .map(CategoryMapper::toCategoryDtoOut)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        //проверяем привязанные события к категории
        long countEvents = eventRepo.findEventsByCategoryId(id);
        if (countEvents == 0) {
            repository.delete(id);
        } else {
            throw new ConflictException(
                    String.format(CATEGORY_WITH_EVENTS_EXCEPTION, id, countEvents));
        }
        repository.delete(id);
    }
}

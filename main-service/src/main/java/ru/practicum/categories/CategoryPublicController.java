package ru.practicum.categories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/categories")
@Validated
@RequiredArgsConstructor
public class CategoryPublicController {

    private final CategoryService service;

    @GetMapping
    public Collection<CategoryDtoOut> getAll(@PositiveOrZero(message = "error")
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @Positive(message = "error")
                                             @RequestParam(defaultValue = "10") Integer size) {
        return service.findAll(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDtoOut find(@PathVariable long catId) {
        return service.find(catId);
    }
}

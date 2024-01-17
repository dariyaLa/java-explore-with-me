package ru.practicum.categories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin/categories")
@Validated
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDtoOut add(@Valid @RequestBody CategoryDto dto) {
        return service.add(dto);
    }

    @PatchMapping("/{catId}")
    public CategoryDtoOut update(@PathVariable long catId, @Valid @RequestBody CategoryDto dto) {
        return service.update(dto, catId);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long catId) {
        service.delete(catId);
    }
}

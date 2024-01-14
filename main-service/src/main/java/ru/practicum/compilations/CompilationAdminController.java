package ru.practicum.compilations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.CompilationUpdateDto;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin/compilations")
@Validated
@RequiredArgsConstructor
public class CompilationAdminController {

    private final CompilationServiceImpl service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDtoOut add(@Valid @RequestBody CompilationDto compilationDto) {
        return service.add(compilationDto);
    }

    @PatchMapping("/{compId}")
    public CompilationDtoOut patch(@PathVariable long compId,
                                   @Valid @RequestBody CompilationUpdateDto compilationUpdateDto) {
        return service.update(compilationUpdateDto, compId);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long compId) {
        service.delete(compId);
    }
}

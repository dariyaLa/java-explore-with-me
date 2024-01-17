package ru.practicum.compilations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/compilations")
@Validated
@RequiredArgsConstructor
public class CompilationPublicController {

    private final CompilationServiceImpl service;

    @GetMapping
    public Collection<CompilationDtoOut> getAll(@RequestParam(required = false) Boolean pinned,
                                                @PositiveOrZero(message = "error")
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @Positive(message = "error")
                                                @RequestParam(defaultValue = "10") Integer size) {
        return service.findAll(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDtoOut getById(@PathVariable long compId) {
        return service.find(compId);
    }
}

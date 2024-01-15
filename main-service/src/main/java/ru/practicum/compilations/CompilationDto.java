package ru.practicum.compilations;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CompilationDto {

    private boolean pinned;
    @NotBlank(message = "Название подборки не может быть пустым")
    @Size(min = 1, max = 50, message = "Название подборки может быть от 1 до 50 символов")
    private String title;
    private final Collection<Long> events = new ArrayList<>();

}

package ru.practicum.categories;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CompilationUpdateDto {

    private Boolean pinned;
    private String title;
    private final List<Long> events = new ArrayList<>();
}

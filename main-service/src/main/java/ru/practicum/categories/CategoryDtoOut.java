package ru.practicum.categories;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDtoOut {
    long id;
    String name;
}

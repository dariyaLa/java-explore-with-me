package ru.practicum.categories;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;

@Builder
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class CategoryDto {

    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 50, message = "Название категории может быть от 1 до 50 символов")
    String name;
}

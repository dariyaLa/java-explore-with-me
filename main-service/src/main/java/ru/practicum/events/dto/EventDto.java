package ru.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.location.LocationDto;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;


@Builder
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class EventDto {

    @NotBlank(message = "Отсутствует текст в аннотации")
    @Size(min = 20, max = 2000, message = "Размер аннотации должен быть от 20 до 2000 символов")
    private String annotation;
    @NotNull
    private Long category;
    @NotBlank(message = "Отсутствует текст в описании")
    @Size(min = 20, max = 7000, message = "Размер описания должен быть от 20 до 7000 символов")
    private String description;
    @NotNull
    @FutureOrPresent(message = "Поле должно содержать дату, которая еще не наступила")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    @NotNull
    private LocationDto location;
    private boolean paid;
    private int participantLimit;
    private Boolean requestModeration;
    @NotBlank(message = "Отсутствует текст в заголовке")
    @Size(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов")
    private String title;
}

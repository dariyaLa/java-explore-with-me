package ru.practicum.requests.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Data
@Builder
public class RequestUpdateDto {

    @NotBlank(message = "Отсутствует новый статус заявок")
    private String status;
    @NotNull(message = "Отсутствует список заявок")
    private Collection<Long> requestIds;
}

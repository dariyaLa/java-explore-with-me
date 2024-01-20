package ru.practicum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse {

    private String stateAction;
}

package ru.practicum.requests.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.requests.RequestState;

import java.time.LocalDateTime;

@Data
@Builder
public class RequestDtoOut {

    private long id;
    private LocalDateTime created;
    private long event;
    private long requester;
    private RequestState status;
}

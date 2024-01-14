package ru.practicum.requests.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
public class RequestUpdateDtoOut {

    private final Collection<RequestDtoOut> confirmedRequests;
    private final Collection<RequestDtoOut> rejectedRequests;


}

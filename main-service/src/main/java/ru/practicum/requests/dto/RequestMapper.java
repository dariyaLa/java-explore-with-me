package ru.practicum.requests.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.requests.Request;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestMapper {

    public static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();

    public static RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .created(LocalDateTime.ofInstant(request.getCreated(), ZONE_OFFSET))
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus())
                .build();
    }

    public static Collection<RequestDto> toRequestDtoOut(Collection<Request> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        return requests.stream().map(RequestMapper::toRequestDto).collect(Collectors.toList());
    }

    public static RequestDtoOut toRequestDtoOut(Request request) {
        return RequestDtoOut.builder()
                .id(request.getId())
                .created(LocalDateTime.ofInstant(request.getCreated(), ZONE_OFFSET))
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus())
                .build();
    }
}

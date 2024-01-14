package ru.practicum.requests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.requests.dto.RequestDtoOut;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@Validated
@RequiredArgsConstructor
public class RequestController {

    private final RequestServiceImpl service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDtoOut add(@PathVariable long userId,
                             @NotNull(message = "Отсутствует id события в запросе")
                             @RequestParam Long eventId) {
        return service.add(userId, eventId);
    }

    @GetMapping
    public Collection<RequestDtoOut> getAll(@PathVariable long userId) {
        return service.findAll(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDtoOut cancel(@PathVariable long userId,
                                @PathVariable long requestId) {
        return service.cancel(userId, requestId);
    }
}

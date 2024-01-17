package ru.practicum.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.Filter;
import ru.practicum.events.dto.EventDto;
import ru.practicum.events.dto.EventDtoOut;
import ru.practicum.events.dto.EventUpdateDto;
import ru.practicum.requests.RequestServiceImpl;
import ru.practicum.requests.dto.RequestDtoOut;
import ru.practicum.requests.dto.RequestUpdateDto;
import ru.practicum.requests.dto.RequestUpdateDtoOut;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventPrivateController {

    private final EventServiceImpl service;
    private final RequestServiceImpl requestService;
    private Filter filter = new Filter();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDtoOut add(@PathVariable long userId,
                           @Valid @RequestBody EventDto eventDto) {
        return service.add(eventDto, userId);
    }

    @GetMapping
    public Collection<EventDtoOut> getByUserId(@PathVariable long userId,
                                               @PositiveOrZero(message = "error")
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @Positive(message = "error")
                                               @RequestParam(defaultValue = "10") Integer size) {
        filter.setMapFilter(userId, from, size);
        return service.findAllByUser(filter.getMapFilter());
    }

    @GetMapping("/{eventId}")
    public EventDtoOut getEventByUserId(@PathVariable long userId,
                                        @PathVariable long eventId) {
        return service.findByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventDtoOut update(@PathVariable long userId,
                              @PathVariable long eventId,
                              @Valid @RequestBody EventUpdateDto updateEventDto) {

        return service.update(userId, eventId, updateEventDto);
    }

    @PatchMapping("/{eventId}/requests")
    public RequestUpdateDtoOut updateRequests(@PathVariable long userId,
                                              @PathVariable long eventId,
                                              @Valid @RequestBody RequestUpdateDto requestUpdateDto) {
        return requestService.update(userId, eventId, requestUpdateDto);
    }

    @GetMapping("/{eventId}/requests")
    public Collection<RequestDtoOut> getRequestsForUsersEvent(@PathVariable long userId,
                                                              @PathVariable long eventId) {
        return requestService.findRequestsForUserByEvent(userId, eventId);
    }
}

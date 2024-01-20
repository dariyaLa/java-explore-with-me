package ru.practicum.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.Filter;
import ru.practicum.constants.Constant;
import ru.practicum.events.dto.EventDtoOut;
import ru.practicum.events.dto.EventUpdateDto;
import ru.practicum.events.enums.EventState;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@Validated
@RequiredArgsConstructor
public class EventAdminController {

    private final EventServiceImpl service;
    private Filter filter;

    @PatchMapping("/{eventId}")
    public EventDtoOut patch(@PathVariable long eventId,
                             @RequestBody EventUpdateDto eventAdminDto) {
        return service.updateByAdmin(eventId, eventAdminDto);
    }

    @GetMapping
    public Collection<EventDtoOut> getAll(@RequestParam(required = false) Collection<Long> users,
                                          @RequestParam(required = false) Collection<String> states,
                                          @RequestParam(required = false) Collection<Long> categories,
                                          @RequestParam(required = false, name = "rangeStart")
                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                          LocalDateTime startLocal,
                                          @RequestParam(required = false, name = "rangeEnd")
                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                          LocalDateTime endLocal,
                                          @PositiveOrZero
                                          @RequestParam(defaultValue = "0") Integer from,
                                          @Positive
                                          @RequestParam(defaultValue = "10") Integer size) {

        if (states != null) {
            states.forEach(EventState::getState);
        }
        Instant start = startLocal == null ? null : startLocal.toInstant(Constant.getZoneOffset());
        Instant end = endLocal == null ? null : endLocal.toInstant(Constant.getZoneOffset());
        filter = new Filter();
        filter.setMapFilter(users, states, categories, start, end, from, size);
        return service.findAllAdminWithFilter(filter.getMapFilter());
    }

}

package ru.practicum.events;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.Filter;
import ru.practicum.events.dto.EventDtoOut;
import ru.practicum.events.enums.EventSort;
import ru.practicum.exception.ValidationException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;

import static ru.practicum.constants.Constant.getZoneOffset;

@Slf4j
@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventServiceImpl service;
    private Filter filter = new Filter();

    @GetMapping
    public Collection<EventDtoOut> getByFiltersPublic(@RequestParam(required = false) String text,
                                                      @RequestParam(required = false) Collection<Long> categories,
                                                      @RequestParam(required = false) Boolean paid,
                                                      @RequestParam(required = false, name = "rangeStart")
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                      LocalDateTime startLocal,
                                                      @RequestParam(required = false, name = "rangeEnd")
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                      LocalDateTime endLocal,
                                                      @RequestParam(defaultValue = "false") boolean onlyAvailable,
                                                      @RequestParam(required = false, name = "sort") String sortParam,
                                                      @PositiveOrZero(message = "error")
                                                      @RequestParam(defaultValue = "0") Integer from,
                                                      @Positive(message = "error")
                                                      @RequestParam(defaultValue = "10") Integer size,
                                                      HttpServletRequest request) {
        EventSort sort = null;
        if (sortParam != null) {
            sort = EventSort.getSort(sortParam);
        }
        Instant start = startLocal == null ? Instant.now() : startLocal.toInstant(getZoneOffset());
        Instant end = endLocal == null ? null : endLocal.toInstant(getZoneOffset());

        if (end != null) {
            validateDate(start, end);
        }
        String ip = request.getRemoteAddr();
        filter.setMapFilter(text, categories, paid, start, end, onlyAvailable, sort, from, size, ip);
        return service.findAllWithFilter(filter.getMapFilter());
    }

    @GetMapping("/{id}")
    public EventDtoOut get(@PathVariable long id, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return service.find(id, ip);
    }

    private static void validateDate(Instant start, Instant end) {
        if (!end.isAfter(start)) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }
    }
}

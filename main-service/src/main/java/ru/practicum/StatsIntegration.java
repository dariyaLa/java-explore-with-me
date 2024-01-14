package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.events.Event;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.constants.Constant.URI_EVENTS;
import static ru.practicum.constants.Constant.getZoneOffset;
import static ru.practicum.events.enums.EventState.PUBLISHED;

//@RequiredArgsConstructor
@Service
@RequiredArgsConstructor
public class StatsIntegration {

    private final StatsClientImpl client;

    public void addHitStats(String uri, String ip, String app) {
        HitDto hitDto = HitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();
        try {
            client.addHit(hitDto);
        } catch (StatsRequestException e) {
            throw new StatsRequestException(
                    String.format("Ошибка добавления просмотра страницы %s пользователем %s: ", uri, ip)
                            + e.getMessage());
        }
    }

    public Collection<ViewStatsDto> statsRequest(Collection<Event> events) {
        if (events.stream().noneMatch(event -> event.getEventState() == PUBLISHED)) {
            return Collections.emptyList();
        }
        List<Event> eventsPublished = events.stream()
                .filter(event -> event.getEventState() == PUBLISHED)
                .collect(Collectors.toList());
        List<String> uris = makeUris(eventsPublished);
        LocalDateTime startStat = getStartTime(eventsPublished);
        boolean unique = true;
        List<String> newUris = uris.stream()
                .map(i -> i.substring(1, i.length()))
                .collect(Collectors.toList());
        return client.getStatistics(startStat.minusHours(1), LocalDateTime.now(), newUris, unique);
    }

    private List<String> makeUris(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        return eventIds.stream().map(id -> "/" + URI_EVENTS + "/" + id).collect(Collectors.toList());
        //return eventIds.stream().map(id -> URI_EVENTS + "/" + id).collect(Collectors.toList());
    }

    private LocalDateTime getStartTime(List<Event> events) {
        Instant startStatInst = events.stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .sorted().collect(Collectors.toList()).get(0);
        return LocalDateTime.ofInstant(startStatInst, getZoneOffset());
    }
}

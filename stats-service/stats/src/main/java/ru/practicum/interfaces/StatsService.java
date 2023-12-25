package ru.practicum.interfaces;

import ru.practicum.HitDto;
import ru.practicum.ViewStatsDto;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public interface StatsService {

    ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    void addHit(@Valid HitDto endpointHitDto);

    List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique);
}

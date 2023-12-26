package ru.practicum.interfaces;

import ru.practicum.model.Hit;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface HitRepository {

    Hit addHit(Hit endpointHit);

    Map<Long, Long> getHitsById(Instant start, Instant end);

    Map<Long, Long> getHitsById(Instant start, Instant end, List<Long> appIds);

    Map<Long, Long> getUniqueHitsByAppId(Instant start, Instant end);

    Map<Long, Long> getUniqueHitsByAppId(Instant start, Instant end, List<Long> appIds);
}

package ru.practicum;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {

    List<ViewStatsDto> getStatistics(LocalDateTime start,
                                     LocalDateTime end,
                                     List<String> uris,
                                     Boolean unique);

    void addHit(HitDto hitDto);
}

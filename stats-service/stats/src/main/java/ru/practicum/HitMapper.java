package ru.practicum;

import ru.practicum.model.Hit;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class HitMapper {
    public static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();

    public static Hit toHit(HitDto hitDto, long appId) {
        return Hit.builder()
                .appId(appId)
                .ip(hitDto.getIp())
                .timestamp(hitDto.getTimestamp().toInstant(ZONE_OFFSET))
                .build();
    }
}

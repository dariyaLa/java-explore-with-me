package ru.practicum;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.interfaces.AppRepository;
import ru.practicum.interfaces.HitRepository;
import ru.practicum.interfaces.StatsService;
import ru.practicum.model.App;
import ru.practicum.model.Hit;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final HitRepository hitRepo;
    private final AppRepository appRepo;

    @Override
    public void addHit(HitDto hitDto) {
        String name = hitDto.getApp();
        String uri = hitDto.getUri();
        long appId;

        List<App> apps = appRepo.findByAppAndUri(name, uri);

        if (apps.isEmpty()) {
            App appToAdd = App.builder()
                    .name(name)
                    .uri(uri)
                    .build();
            appId = appRepo.add(appToAdd);
        } else {
            appId = apps.get(0).getId();
        }

        Hit endpointHit = HitMapper.toHit(hitDto, appId);
        hitRepo.addHit(endpointHit);
        log.debug("Пользователь {} просмотрел uri {} сервиса {}", endpointHit.getIp(), uri, name);

    }

    @Override
    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        String startDecoded = decodeDateTime(start);
        String endDecoded = decodeDateTime(end);

        Instant startDate = parseDateTime(startDecoded);
        Instant endDate = parseDateTime(endDecoded);

        Map<Long, Long> viewsByAppId;
        List<App> apps;
        List<Long> appIds;

        if (uris.isEmpty()) {
            viewsByAppId = unique ? hitRepo.getUniqueHitsByAppId(startDate, endDate) : hitRepo.getHitsById(startDate, endDate);
            if (viewsByAppId.isEmpty()) {
                log.debug("Не найдены данные для периода с {} по {}. Уникальные просмотры {}",
                        start, end, unique);
                return Collections.emptyList();
            }
            appIds = new ArrayList<>(viewsByAppId.keySet());
            apps = appRepo.getAppsByIds(appIds);
        } else {
            apps = appRepo.getAppsByUris(uris);
            if (apps.isEmpty()) {
                log.debug("Не найдены данные для периода с {} по {}. Список uri {}. Уникальные просмотры {}",
                        start, end, uris, unique);
                return Collections.emptyList();
            }
            appIds = apps.stream().map(App::getId).collect(Collectors.toList());
            viewsByAppId = unique ? hitRepo.getUniqueHitsByAppId(startDate, endDate, appIds) :
                    hitRepo.getHitsById(startDate, endDate, appIds);
            if (viewsByAppId.isEmpty()) {
                log.debug("Не найдены данные для периода с {} по {}. Список uri {}. Уникальные просмотры {}",
                        startDate, endDate, uris, unique);
                return Collections.emptyList();
            }
        }
        log.debug("Статистика для периода с {} по {}. Список uri {}. Уникальные просмотры {}",
                startDate, endDate, uris, unique);
        return AppMapper.toViewStatsDtoList(apps, viewsByAppId);
    }


    public static String decodeDateTime(String dateTime) {
        return URLDecoder.decode(dateTime, StandardCharsets.UTF_8);
    }

    public static Instant parseDateTime(String dateTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
        return localDateTime.toInstant(ZONE_OFFSET);
    }
}

package ru.practicum;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsClientImpl implements StatsClient {

    private final RestTemplate rest;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClientImpl() {
        rest = new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory("${stats-server.URL}"))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    @Override
    public List<ViewStatsDto> getStatistics(LocalDateTime start,
                                            LocalDateTime end,
                                            List<String> uris,
                                            Boolean unique) {

        Map<String, Object> parameters = addDateParameters(start, end);
        parameters.put("uris", String.join(",", uris));
        parameters.put("unique", unique);
        String url = "${stats-server.PATH_PARAM_WITH_DATE}" + "${stats-server.PATH_PARAM_WITH_URIS}"
                + "${stats-server.PATH_PARAM_WITH_UNIQUE}";
        return sendRequest(url, parameters);
    }

    @Override
    public void addHit(HitDto hitDto) {

        HttpEntity<HitDto> request = new HttpEntity<>(hitDto);
        ResponseEntity<Void> response = rest.exchange("${stats-server.PATH_HIT}", HttpMethod.POST,
                request, Void.class);
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new StatsRequestException("Ошибка при сохранении данных: " + response.getBody());
        }

    }

    private List<ViewStatsDto> sendRequest(String url, Map<String, Object> parameters) {
        ViewStatsDto[] response;
        try {
            response = rest.getForObject(url, ViewStatsDto[].class, parameters);
        } catch (RuntimeException e) {
            throw new StatsRequestException("Ошибка при запросе данных статистики: " + e.getMessage());
        }
        if (response == null) {
            throw new StatsRequestException("Ошибка при запросе данных статистики");
        }
        return Arrays.asList(response);
    }

    private Map<String, Object> addDateParameters(LocalDateTime start, LocalDateTime end) {
        String startEncode = encodeDate(start);
        String endEncode = encodeDate(end);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", startEncode);
        parameters.put("end", endEncode);
        return parameters;
    }

    private String encodeDate(LocalDateTime dateTime) {
        String dateTimeString = dateTime.format(FORMATTER);
        return URLEncoder.encode(dateTimeString, StandardCharsets.UTF_8);
    }
}

package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Transactional
@AutoConfigureTestDatabase
@RequiredArgsConstructor
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class StatsServiceImplTest {

    private HitDto hitDto;

    @Autowired
    private StatsServiceImpl service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @SneakyThrows
    void addHitTest() {
        service.addHit(hitDto);
        String queryApp = "SELECT count(*) FROM apps";
        String queryHit = "SELECT count(*) FROM hits";
        Integer resultApp = jdbcTemplate.queryForObject(queryApp, Integer.class);
        Integer resultHit = jdbcTemplate.queryForObject(queryHit, Integer.class);
        assertEquals(resultApp, 1, "Количество строк select-а не соответствует ожидаемому");
        assertEquals(resultHit, 1, "Количество строк select-а не соответствует ожидаемому");

    }

    @BeforeEach
    void setUp() {

        hitDto = HitDto.builder()
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.163.0.1")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
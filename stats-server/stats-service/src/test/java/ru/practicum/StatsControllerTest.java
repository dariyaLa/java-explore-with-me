package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StatsControllerTest {

    @InjectMocks
    private StatsController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Mock
    private StatsServiceImpl service;

    private HitDto hitDto;

    private ViewStatsDto viewStatsDto;

    private List<ViewStatsDto> viewStatsDtoList = new ArrayList<>();

    @Test
    @SneakyThrows
    void addHitTest() {

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(hitDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @SneakyThrows
    void getStatTest() {
        when(service.getStats(anyString(), anyString(), anyList(), anyBoolean()))
                .thenReturn(viewStatsDtoList);

        mvc.perform(get("/stats?start=2020-05-05 00:00:00&end=2035-05-05 00:00:00")
                        .content(mapper.writeValueAsString(viewStatsDtoList))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(viewStatsDtoList.size())))
                .andExpect(jsonPath("$.[0].app", is(viewStatsDtoList.get(0).getApp()), String.class))
                .andExpect(jsonPath("$.[0].uri", is(viewStatsDtoList.get(0).getUri()), String.class))
                .andExpect(jsonPath("$.[0].hits", is(viewStatsDtoList.get(0).getHits()), Long.class));
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        mapper.registerModule(new JavaTimeModule());

        hitDto = HitDto.builder()
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.163.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        viewStatsDto = ViewStatsDto.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(1L)
                .build();

        viewStatsDtoList.add(viewStatsDto);
    }

}
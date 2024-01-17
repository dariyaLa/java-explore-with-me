package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@PropertySource("classpath:application.properties")
@SpringBootApplication
public class StatsService {
    public static void main(String[] args) {

        SpringApplication.run(StatsService.class, args);

    }
}

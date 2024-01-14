package ru.practicum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@Getter
@PropertySource("classpath:application.properties")
//@Configuration
//@ConfigurationProperties(prefix = "stats.server")
//@AllArgsConstructor
@RequiredArgsConstructor
public class AppConfig {

    @Getter
    @Value("${stats-server.url}")
    private String url;

}

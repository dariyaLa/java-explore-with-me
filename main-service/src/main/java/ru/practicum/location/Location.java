package ru.practicum.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Location {

    private long id;
    private float lat;
    private float lon;

    public Map<String, Object> toMap() {
        return Map.of(
                "lat", lat,
                "lon", lon
        );
    }
}

package ru.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.categories.CategoryDtoOut;
import ru.practicum.events.enums.EventState;
import ru.practicum.location.LocationDto;
import ru.practicum.users.UserDtoOut;

import java.time.LocalDateTime;

@Data
@Builder
public class EventDtoOut {

    private long id;
    private String annotation;
    private CategoryDtoOut category;
    private int confirmedRequests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private UserDtoOut initiator;
    private LocationDto location;
    private boolean paid;
    private int participantLimit;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private EventState state;
    private String title;
    private long views;
}

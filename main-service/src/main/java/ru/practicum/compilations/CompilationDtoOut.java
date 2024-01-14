package ru.practicum.compilations;

import lombok.Builder;
import lombok.Data;
import ru.practicum.events.dto.EventShortDtoOut;

import java.util.ArrayList;
import java.util.Collection;

@Data
@Builder
public class CompilationDtoOut {

    private long id;
    private boolean pinned;
    private String title;
    private Collection<EventShortDtoOut> events;

    public void setEvents(Collection<EventShortDtoOut> newEvents) {
        if (newEvents != null && !newEvents.isEmpty()) {
            events = new ArrayList<>(newEvents);
        } else {
            events = new ArrayList<>();
        }
    }


}

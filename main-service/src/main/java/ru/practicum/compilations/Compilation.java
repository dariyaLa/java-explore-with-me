package ru.practicum.compilations;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Compilation {

    private long id;
    private String title;
    private boolean pinned;
    private Collection<Long> events;

    public void setEvents(Collection<Long> newEvents) {
        if (newEvents != null && !newEvents.isEmpty()) {
            events = new ArrayList<>(newEvents);
        } else {
            events = new ArrayList<>();
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("pinned", pinned);
        return map;
    }
}

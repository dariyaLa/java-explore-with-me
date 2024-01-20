package ru.practicum;

import ru.practicum.events.enums.EventSort;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.practicum.constants.Constant.*;

public class Filter {


    private final Map<String, Object> mapFilter = new HashMap<>();

    public Map<String, Object> getMapFilter() {
        return mapFilter;
    }

    public void setMapFilter(Collection<Long> users,
                             Collection<String> states,
                             Collection<Long> categories,
                             Instant startLocal,
                             Instant endLocal,
                             Integer from,
                             Integer size) {
        mapFilter.put(USERS, users);
        mapFilter.put(STATES, states);
        mapFilter.put(CATEGORIES, categories);
        mapFilter.put(START, startLocal);
        mapFilter.put(END, endLocal);
        mapFilter.put(FROM, from);
        mapFilter.put(SIZE, size);
        mapFilter.put(IS_PUBLISHED, null);
        mapFilter.put(TEXT, null);
        mapFilter.put(PAID, null);
        mapFilter.put(SORT, null);

    }

    public void setMapFilter(String text,
                             Collection<Long> categories,
                             Boolean paid,
                             Instant startLocal,
                             Instant endLocal,
                             Boolean onlyAvailable,
                             EventSort sort,
                             Integer from,
                             Integer size,
                             String ip) {
        mapFilter.put(TEXT, text);
        mapFilter.put(CATEGORIES, categories);
        mapFilter.put(PAID, paid);
        mapFilter.put(START, startLocal);
        mapFilter.put(END, endLocal);
        mapFilter.put(ONLY_AVAILABLE, onlyAvailable);
        mapFilter.put(SORT, sort);
        mapFilter.put(FROM, from);
        mapFilter.put(SIZE, size);
        mapFilter.put(IP, ip);
    }

    public void setMapFilter(Long userId,
                             Integer from,
                             Integer size) {
        mapFilter.put(USER, userId);
        mapFilter.put(FROM, from);
        mapFilter.put(SIZE, size);

    }
}

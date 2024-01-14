package ru.practicum.events.enums;

import ru.practicum.exception.StateException;

public enum EventSort {

    EVENT_DATE,
    VIEWS;

    public static EventSort getSort(String state) {
        try {
            return EventSort.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new StateException("Unknown state: UNSUPPORTED_STATUS " + state);
        }
    }

}

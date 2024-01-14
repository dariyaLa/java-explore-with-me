package ru.practicum.events.enums;

import ru.practicum.exception.StateException;

public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED;

    public static EventState getState(String state) {
        try {
            return EventState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new StateException("Unknown state: UNSUPPORTED_STATUS " + state);
        }
    }
}

package ru.practicum.events.enums;

import ru.practicum.exception.StateException;

public enum EventStatePrivate {
    SEND_TO_REVIEW,
    CANCEL_REVIEW;

    public static EventStatePrivate getState(String state) {
        try {
            return EventStatePrivate.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new StateException("Unknown state: UNSUPPORTED_STATUS " + state);
        }
    }
}

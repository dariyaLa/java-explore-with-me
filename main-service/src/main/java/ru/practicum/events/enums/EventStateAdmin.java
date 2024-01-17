package ru.practicum.events.enums;

import ru.practicum.exception.StateException;

public enum EventStateAdmin {
    PUBLISH_EVENT,
    REJECT_EVENT;

    public static EventStateAdmin getState(String state) {
        try {
            return EventStateAdmin.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new StateException("Unknown state: UNSUPPORTED_STATUS " + state);
        }
    }

}

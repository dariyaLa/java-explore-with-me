package ru.practicum.requests;

import ru.practicum.exception.StateException;

public enum RequestState {

    PENDING,
    CONFIRMED,
    REJECTED,
    CANCELED;

    public static RequestState getState(String state) {
        try {
            return RequestState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new StateException("Unknown state: UNSUPPORTED_STATUS " + state);
        }
    }
}

package ru.practicum.subscribers;

import ru.practicum.exception.StateException;

public enum SubscriptionState {
    ACTIVE,
    OPENED,
    CLOSED;

    public static SubscriptionState getState(String state) {
        try {
            return SubscriptionState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new StateException("Unknown state: UNSUPPORTED_STATUS " + state);
        }
    }
}

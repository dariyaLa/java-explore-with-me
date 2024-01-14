package ru.practicum;

import java.util.Collection;

public interface ServiceRequests<T, K, L, G> {

    T add(K objO, K objT);

    Collection<T> findAll(Long userId);

    Collection<T> findRequestsForUserByEvent(Long userId, Long eventId);

    T cancel(Long userId, Long reqId);

    L update(Long userId, Long eventId, G obj);
}

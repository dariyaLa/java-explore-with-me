package ru.practicum;

import java.util.Collection;

public interface SubscriptionRepository<T, K> {

    T add(T subscription);

    T find(long id);

    void update(long id, long userId, K state);

    Collection<T> findAllBySubscriber(long userId, Integer from, Integer size);

    Collection<T> findAllByUser(long userId, Integer from, Integer size);

    void closedSubscription(long closedUserId, long userId, Boolean isSubscriber);
}

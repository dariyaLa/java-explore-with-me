package ru.practicum;

import java.util.Collection;

public interface SubscriptionService<T, K, H> {

    T add(K obj, long userId);

    void update(long subscriptionId, long userId, boolean isApprovedSubscription);

    Collection<T> findAllBySubscriber(long userId, Integer from, Integer size);

    Collection<H> findAllByUser(long userId, Integer from, Integer size);

    void closedSubscription(long closedUserId, long userId, boolean isSubscriber);

}

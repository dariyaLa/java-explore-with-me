package ru.practicum.subscribers;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Subscription {

    private long id;
    private long userSubscription; //id user, на которого хотим подписаться
    private long subscriber; //подписчик
    private SubscriptionState subscriptionState; //статус подписки
    private Instant created; //когда подписались
    private Instant closed; //когда отписались

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId_subscription", userSubscription);
        map.put("subscriber_id", subscriber);
        map.put("state", subscriptionState);
        return map;
    }
}

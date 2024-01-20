package ru.practicum.subscribers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.subscribers.dto.SubscribersDtoOut;
import ru.practicum.subscribers.dto.SubscriptionDtoOut;
import ru.practicum.users.User;
import ru.practicum.users.UserMapper;

import java.time.LocalDateTime;

import static ru.practicum.constants.Constant.getZoneOffset;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscriptionMapper {

    public static Subscription toSubscription(long userId, long userSubscription, SubscriptionState subscriptionState) {
        return Subscription.builder()
                .userSubscription(userSubscription)
                .subscriber(userId)
                .subscriptionState(subscriptionState)
                .build();
    }

    public static SubscriptionDtoOut toSubscriptionDtoOut(Subscription subscription,
                                                          User userSubscription) {
        return SubscriptionDtoOut.builder()
                .id(subscription.getId())
                .userSubscription(UserMapper.toUserDto(userSubscription))
                .subscriptionState(subscription.getSubscriptionState())
                .createdOn(LocalDateTime.ofInstant(subscription.getCreated(), getZoneOffset()))
                .closedOn(subscription.getClosed() != null ? LocalDateTime.ofInstant(subscription.getClosed(), getZoneOffset()) : null)
                .build();

    }

    public static SubscribersDtoOut toSubscribersDtoOut(Subscription subscription,
                                                        User subscriber) {
        return SubscribersDtoOut.builder()
                .id(subscription.getId())
                .subscriber(UserMapper.toUserDto(subscriber))
                .subscriptionState(subscription.getSubscriptionState())
                .createdOn(LocalDateTime.ofInstant(subscription.getCreated(), getZoneOffset()))
                .closedOn(subscription.getClosed() != null ? LocalDateTime.ofInstant(subscription.getClosed(), getZoneOffset()) : null)
                .build();

    }
}

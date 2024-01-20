package ru.practicum.subscribers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.SubscriptionService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.PrivateProfile;
import ru.practicum.subscribers.dto.SubscribersDtoOut;
import ru.practicum.subscribers.dto.SubscriptionDto;
import ru.practicum.subscribers.dto.SubscriptionDtoOut;
import ru.practicum.users.User;
import ru.practicum.users.UserRepoImpl;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService<SubscriptionDtoOut, SubscriptionDto, SubscribersDtoOut> {

    private final SubscriptionRepoImpl repository;
    private final UserRepoImpl userRepo;

    @Override
    public SubscriptionDtoOut add(SubscriptionDto subscriptionDto, long subscriber) {
        //находим подписчика
        User user = userRepo.find(subscriber);
        //находим пользователя, на кот. хотим подписаться
        User userSubscription = userRepo.findByName(subscriptionDto.getUserSubscription());
        //проверка корректности user-ов
        checkUsersSubscription(userSubscription.getId(), subscriber);
        //добавляем подписку
        Subscription newSubscription = repository.add(SubscriptionMapper.toSubscription(
                user.getId(),
                userSubscription.getId(),
                userSubscription.isPublicProfile() ? SubscriptionState.ACTIVE : SubscriptionState.OPENED));
        if (userSubscription.isPublicProfile()) {
            return SubscriptionMapper.toSubscriptionDtoOut(newSubscription, userSubscription);
        } else {
            throw new PrivateProfile(String.format("У пользователя %s закрытый профиль, " +
                    "подписка автоматически активируется, когда пользователь ее одобрит", userSubscription.getName()),
                    newSubscription.getId());
        }
    }

    @Override
    public void update(long subscriptionId, long userId, boolean isApprovedSubscription) {
        SubscriptionState state = null;
        if (isApprovedSubscription) {
            state = SubscriptionState.ACTIVE;
        }
        repository.update(subscriptionId, userId, state);
    }

    //ищем все подписки подписчика
    @Override
    public Collection<SubscriptionDtoOut> findAllBySubscriber(long userId, Integer from, Integer size) {
        return repository.findAllBySubscriber(userId, from, size).stream()
                .map(i -> {
                    User userSubscription = userRepo.find(i.getUserSubscription());
                    return SubscriptionMapper.toSubscriptionDtoOut(i, userSubscription);
                })
                .collect(Collectors.toList());

    }

    //ищем все OPEN подписки, которые требуют одобрения
    @Override
    public Collection<SubscribersDtoOut> findAllByUser(long userId, Integer from, Integer size) {
        return repository.findAllByUser(userId, from, size).stream()
                .map(i -> {
                    User subscriber = userRepo.find(i.getSubscriber());
                    return SubscriptionMapper.toSubscribersDtoOut(i, subscriber);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void closedSubscription(long closedUser, long userId, boolean isSubscriber) {
        repository.closedSubscription(closedUser, userId, isSubscriber);
    }

    private void checkUsersSubscription(long userId, long subscriberId) {
        if (userId == subscriberId) {
            throw new ConflictException("Невозможно создать подписку на самого себя");
        }
    }

}

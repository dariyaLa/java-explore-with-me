package ru.practicum.subscribers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ApiResponse;
import ru.practicum.subscribers.dto.SubscribersDtoOut;
import ru.practicum.subscribers.dto.SubscriptionDto;
import ru.practicum.subscribers.dto.SubscriptionDtoOut;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

import static ru.practicum.constants.Constant.SUCCESSFUL;

@Slf4j
@RestController
@RequestMapping(path = "/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionServiceImpl service;

    @PostMapping(path = "/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionDtoOut add(@PathVariable @NotNull long userId,
                                  @Valid @RequestBody SubscriptionDto subscriptionDto) {
        return service.add(subscriptionDto, userId);
    }

    @PostMapping(path = "/approved/{subscriptionsId}/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse update(@RequestParam @NotNull boolean isApprovedSubscription,
                              @PathVariable @NotNull long subscriptionsId,
                              @PathVariable @NotNull long userId) {
        service.update(subscriptionsId, userId, isApprovedSubscription);
        return ApiResponse.builder().stateAction(SUCCESSFUL).build();
    }

    @PostMapping(path = "/cancelled/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse cansel(@PathVariable @NotNull long userId, //пользователь, от которого запрос о закрытии
                              @RequestParam @NotNull long userIdForClosed,
                              @RequestParam @NotNull boolean isSubscriber) {
        //если isSubscriber - true, подписчик отписывается
        //если false - пользователь удаляет подписчика
        service.closedSubscription(userIdForClosed, userId, isSubscriber);
        return ApiResponse.builder().stateAction(SUCCESSFUL).build();
    }

    @GetMapping(path = "/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<SubscriptionDtoOut> findAllBySubscriber(@PathVariable @NotNull long userId,
                                                              @PositiveOrZero
                                                              @RequestParam(defaultValue = "0") Integer from,
                                                              @Positive
                                                              @RequestParam(defaultValue = "10") Integer size) {
        return service.findAllBySubscriber(userId, from, size);
    }

    @GetMapping(path = "/requests/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<SubscribersDtoOut> findAllByUser(@PathVariable @NotNull long userId,
                                                       @PositiveOrZero
                                                       @RequestParam(defaultValue = "0") Integer from,
                                                       @Positive
                                                       @RequestParam(defaultValue = "10") Integer size) {
        return service.findAllByUser(userId, from, size);
    }
}

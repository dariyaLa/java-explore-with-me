package ru.practicum.subscribers.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Builder
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class SubscriptionDto {

    @NotBlank
    private String userSubscription; //user, на которого хотим подписаться
}

package ru.practicum.requests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ServiceRequests;
import ru.practicum.events.Event;
import ru.practicum.events.EventRepoImpl;
import ru.practicum.events.enums.EventState;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.requests.dto.RequestDtoOut;
import ru.practicum.requests.dto.RequestMapper;
import ru.practicum.requests.dto.RequestUpdateDto;
import ru.practicum.requests.dto.RequestUpdateDtoOut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.practicum.constants.Constant.*;
import static ru.practicum.requests.RequestState.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements ServiceRequests<RequestDtoOut, Long, RequestUpdateDtoOut, RequestUpdateDto> {

    private final RequestRepositoryImpl repository;
    private final EventRepoImpl eventRepo;

    @Override
    public RequestDtoOut add(Long userId, Long eventId) {
        Event event = eventRepo.find(eventId);
        validateRequest(event, eventId, userId);
        Request request = Request.builder()
                .requesterId(userId)
                .eventId(eventId)
                .build();
        int confirmedRequests = repository.countConfirmedRequestsByEventId(eventId);
        int limit = event.getParticipantLimit();
        if (!event.isRequestModeration() && (confirmedRequests < limit) || limit == 0) {
            request.setStatus(CONFIRMED);
        } else {
            request.setStatus(RequestState.PENDING);
        }
        try {
            request = repository.add(request);
        } catch (RuntimeException e) {
            throw new ConflictException(String.format(REQUEST_DUPLICATE_EXCEPTION, event.getId(), userId));
        }
        return RequestMapper.toRequestDtoOut(request);
    }

    @Override
    public Collection<RequestDtoOut> findAll(Long userId) {
        Collection<Request> requests = repository.findAllByUser(userId);
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        return requests.stream()
                .map(RequestMapper::toRequestDtoOut)
                .collect(Collectors.toList());
    }

    @Override
    public RequestDtoOut cancel(Long userId, Long reqId) {
        Request request = repository.find(reqId);
        if (request.getRequesterId() != userId) {
            throw new NotFoundException(
                    String.format("Редактирование запроса с id %d недоступно для пользователя с id %d",
                            reqId, userId));
        }
        repository.cancel(reqId);
        request.setStatus(CANCELED);
        return RequestMapper.toRequestDtoOut(request);
    }

    @Override
    public RequestUpdateDtoOut update(Long userId, Long eventId, RequestUpdateDto requestUpdateDto) {
        String statusParam = requestUpdateDto.getStatus();
        RequestState newStatus = RequestState.getState(statusParam);
        Event event = eventRepo.find(eventId);
        int participantLimit = event.getParticipantLimit();
        if (participantLimit == 0 || !event.isRequestModeration()) {
            throw new ConflictException(String.format("Событие с id %d не требует одобрения заявок", event.getId()));
        }
        //проверяем лимит
        int countConfirmedRequests = repository.countConfirmedRequestsByEventId(eventId);
        if (countConfirmedRequests >= participantLimit) {
            throw new ConflictException(String.format(EVENT_OVERFULL_PARTICIPANT_EXCEPTION, eventId));
        }

        Collection<Long> requestIds = requestUpdateDto.getRequestIds();
        List<Request> requestsToUpdate = new ArrayList<>(repository.findByIds(requestIds));

        requestsToUpdate.forEach(request -> {
            if (request.getEventId() != eventId) {
                throw new NotFoundException(
                        String.format("Заявка с id %d не относится к событию с id %d", request.getId(), eventId));
            }
            if (request.getStatus() != RequestState.PENDING) {
                throw new ConflictException("Можно изменить статус только у заявок, находящихся в состоянии ожидания");
            }
        });

        if (newStatus == CONFIRMED) {
            int confirmedBeforeRequestsQty = repository.countConfirmedRequestsByEventId(eventId);
            if (confirmedBeforeRequestsQty >= event.getParticipantLimit()) {
                throw new ConflictException(String.format(EVENT_OVERFULL_PARTICIPANT_EXCEPTION, eventId));
            }

            List<Request> confirmedRequests;
            List<Request> rejectedRequests = new ArrayList<>();

            int requestsQty = requestsToUpdate.size();
            int freeQtyToConfirm = participantLimit - confirmedBeforeRequestsQty;

            if (freeQtyToConfirm >= requestsQty) {
                requestsToUpdate.forEach(request -> request.setStatus(CONFIRMED));
                confirmedRequests = requestsToUpdate;
                repository.updateStates(requestIds, CONFIRMED);
            } else {
                IntStream.range(0, freeQtyToConfirm).forEach(i -> requestsToUpdate.get(i).setStatus(CONFIRMED));
                IntStream.range(freeQtyToConfirm, requestsQty).forEach(i -> requestsToUpdate.get(i).setStatus(REJECTED));
                confirmedRequests = requestsToUpdate.stream().limit(freeQtyToConfirm).collect(Collectors.toList());
                rejectedRequests = requestsToUpdate.stream().skip(freeQtyToConfirm).collect(Collectors.toList());
                repository.updateStates(confirmedRequests.stream()
                        .map(Request::getId).collect(Collectors.toList()), CONFIRMED);
                repository.updateStates(rejectedRequests.stream()
                        .map(Request::getId).collect(Collectors.toList()), REJECTED);
            }

            Collection<RequestDtoOut> confirmedRequestsOut = confirmedRequests.stream()
                    .map(RequestMapper::toRequestDtoOut)
                    .collect(Collectors.toList());
            Collection<RequestDtoOut> rejectedRequestsOut = rejectedRequests.stream()
                    .map(RequestMapper::toRequestDtoOut)
                    .collect(Collectors.toList());

            return new RequestUpdateDtoOut(confirmedRequestsOut, rejectedRequestsOut);

        } else if (newStatus == REJECTED) {
            requestsToUpdate.forEach(request -> request.setStatus(REJECTED));
            repository.updateStates(requestIds, REJECTED);
            Collection<RequestDtoOut> requestsToUpdateOut = requestsToUpdate.stream()
                    .map(RequestMapper::toRequestDtoOut)
                    .collect(Collectors.toList());
            return new RequestUpdateDtoOut(Collections.emptyList(), requestsToUpdateOut);
        } else {
            throw new ValidationException("Нельзя изменить статус на PENDING");
        }
    }

    @Override
    public Collection<RequestDtoOut> findRequestsForUserByEvent(Long userId, Long eventId) {
        Event event = eventRepo.find(eventId);
        if (event.getInitiator() != userId) {
            throw new ValidationException(
                    String.format("Пользователь с id %d не организатор события с id %d", userId, eventId));
        }
        Collection<Request> requests = repository.findByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::toRequestDtoOut)
                .collect(Collectors.toList());
    }

    private void validateRequest(Event event, long eventId, long userId) {
        int confirmedRequests = repository.countConfirmedRequestsByEventId(eventId);
        int limit = event.getParticipantLimit();
        if (limit != 0 && confirmedRequests >= limit) {
            throw new ConflictException(String.format(EVENT_OVERFULL_PARTICIPANT_EXCEPTION, eventId));
        }
        if (event.getInitiator() == userId) {
            throw new ConflictException(String.format(REQUEST_INITIATOR_EXCEPTION, eventId, userId));
        }
        if (event.getEventState() != EventState.PUBLISHED) {
            throw new ConflictException(
                    String.format("Нельзя участвовать в неопубликованном событии с id %d", eventId));
        }
    }
}

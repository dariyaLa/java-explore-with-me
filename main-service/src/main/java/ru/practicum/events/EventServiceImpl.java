package ru.practicum.events;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ServiceEvents;
import ru.practicum.StatsIntegration;
import ru.practicum.StatsRequestException;
import ru.practicum.ViewStatsDto;
import ru.practicum.categories.Category;
import ru.practicum.categories.CategoryRepoImpl;
import ru.practicum.events.dto.EventDto;
import ru.practicum.events.dto.EventDtoOut;
import ru.practicum.events.dto.EventMapper;
import ru.practicum.events.dto.EventUpdateDto;
import ru.practicum.events.enums.EventSort;
import ru.practicum.events.enums.EventState;
import ru.practicum.events.enums.EventStateAdmin;
import ru.practicum.events.enums.EventStatePrivate;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.Location;
import ru.practicum.location.LocationDto;
import ru.practicum.location.LocationMapper;
import ru.practicum.location.LocationRepoImpl;
import ru.practicum.requests.RequestRepositoryImpl;
import ru.practicum.users.User;
import ru.practicum.users.UserRepoImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.constants.Constant.*;
import static ru.practicum.events.enums.EventState.PUBLISHED;

@Slf4j
@Service
@AllArgsConstructor
public class EventServiceImpl implements ServiceEvents<EventDto, EventDtoOut> {

    private final EventRepoImpl repository;
    private final UserRepoImpl userRepo;
    private final LocationRepoImpl locationRepo;
    private final CategoryRepoImpl categoryRepo;
    private final RequestRepositoryImpl requestRepo;
    private final StatsIntegration statsIntegration;

    @Override
    public EventDtoOut add(EventDto eventDto, Long userId) {
        if (eventDto.getRequestModeration() == null) {
            eventDto.setRequestModeration(true);
        }
        Location location = LocationMapper.toLocation(eventDto.getLocation());
        Location newLocation = addLocation(location);
        long locationId = newLocation.getId();
        Event event = EventMapper.toEvent(eventDto, locationId, userId);
        event = repository.add(event);
        return eventFullDto(event);
    }

    @Override
    public Collection<EventDtoOut> findAllAdminWithFilter(Map<String, Object> mapFilter) {
        Collection<Event> events = repository.findAllWithFilter(mapFilter);
        return eventsFullDtoOut(events);
    }

    @Override
    public Collection<EventDtoOut> findAllWithFilter(Map<String, Object> mapFilter) {
        if (mapFilter.get(TEXT) != null) {
            String text = (String) mapFilter.get(TEXT);
            text = text.toLowerCase();
            mapFilter.put(TEXT, text);
        }
        //устанавливаем и published для критерия поиска
        mapFilter.put(IS_PUBLISHED, true);

        Collection<Event> events = repository.findAllWithFilter(mapFilter);
        statsIntegration.addHitStats(URI_EVENTS, (String) mapFilter.get(IP), APP_MAIN);
        if ((Boolean) mapFilter.get(ONLY_AVAILABLE)) {
            Collection<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
            Map<Long, Integer> confirmedRequestsQty = requestRepo.countConfirmedRequestsByEventIds(eventIds);
            events = events.stream()
                    .filter(event -> confirmedRequestsQty.get(event.getId()) < event.getParticipantLimit())
                    .collect(Collectors.toList());
        }
        if (mapFilter.get(SORT) == EventSort.VIEWS) {
            return eventsFullSortedByViewsDtoList(events, (Integer) mapFilter.get(FROM), (Integer) mapFilter.get(SIZE));

        }
        events.forEach(event -> statsIntegration.addHitStats(URI_EVENTS + "/" + event.getId(), (String) mapFilter.get(IP), APP_MAIN));
        return eventsFullDtoOutList(events);
    }


    @Override
    public Collection<EventDtoOut> findAllByUser(Map<String, Object> mapFilter) {
        Collection<Event> events = repository.findAllByUser(mapFilter);
        return eventsFullDtoOut(events);
    }

    @Override
    public EventDtoOut find(Long id, String ip) {
        Event event = repository.find(id);
        if (event.getEventState() != PUBLISHED) {
            throw new NotFoundException(String.format("Событие с id %d еще не опубликовано", id));
        }
        statsIntegration.addHitStats(URI_EVENTS + "/" + id, ip, APP_MAIN);
        return eventFullDto(event);
    }

    @Override
    public EventDtoOut findByUser(Long userId, Long eventId) {
        Event event = repository.find(eventId);
        checkUser(event, userId);
        return eventFullDto(event);
    }

    @Override
    public EventDtoOut update(Long userId, Long eventId, EventUpdateDto eventDto) {
        Event event = repository.find(eventId);
        if (event.getEventState() == PUBLISHED) {
            throw new ConflictException(String.format(EVENT_PUBLISHED_EDIT_EXCEPTION, eventId));
        }
        checkUser(event, userId);
        updateFields(event, eventDto);
        String stateAction = eventDto.getStateAction();
        if (stateAction != null) {
            setEventStateByPrivateAction(event, stateAction);
        }
        event = repository.update(event);
        return eventFullDto(event);
    }

    @Override
    public EventDtoOut updateByAdmin(Long eventId, EventUpdateDto updateEventDto) {
        Event event = repository.find(eventId);
        updateFields(event, updateEventDto);
        String stateAction = updateEventDto.getStateAction();
        if (stateAction != null) {
            if (event.getEventState() != EventState.PENDING) {
                throw new ConflictException("Событие можно опубликовать или отклонить только в статусе PENDING");
            }
            setEventStateByAdminAction(event, stateAction);
        }
        event = repository.update(event, eventId);
        return eventFullDto(event);
    }

    //добавление локации
    public Location addLocation(Location location) {
        return locationRepo.add(location);
    }

    private void setEventStateByAdminAction(Event event, String stateAction) {
        EventStateAdmin action = EventStateAdmin.valueOf(stateAction);
        if (action == EventStateAdmin.PUBLISH_EVENT) {
            event.setEventState(PUBLISHED);
            event.setPublishedOn(Instant.now());
        } else {
            event.setEventState(EventState.CANCELED);
        }
    }

    private void updateFields(Event event, EventUpdateDto updateEventAdminDto) {
        String annotationToUpdate = updateEventAdminDto.getAnnotation();
        Long categoryToUpdate = updateEventAdminDto.getCategory();
        String descriptionToUpdate = updateEventAdminDto.getDescription();
        LocalDateTime eventDateToUpdate = updateEventAdminDto.getEventDate();
        LocationDto locationToUpdate = updateEventAdminDto.getLocation();
        Boolean paidToUpdate = updateEventAdminDto.getPaid();
        Integer participantLimitToUpdate = updateEventAdminDto.getParticipantLimit();
        Boolean requestModerationToUpdate = updateEventAdminDto.getRequestModeration();
        String titleToUpdate = updateEventAdminDto.getTitle();

        if (annotationToUpdate != null) {
            validateStringField(annotationToUpdate, "аннотация", 20, 2000);
            event.setAnnotation(annotationToUpdate);
        }
        if (categoryToUpdate != null) {
            validatePositive(Long.bitCount(categoryToUpdate), "id категории");
            event.setCategoryId(categoryToUpdate);
        }
        if (descriptionToUpdate != null) {
            validateStringField(descriptionToUpdate, "описание", 20, 7000);
            event.setDescription(descriptionToUpdate);
        }
        if (eventDateToUpdate != null) {
            validateEventDate(eventDateToUpdate);
            event.setEventDate(eventDateToUpdate.toInstant(getZoneOffset()));
        }
        if (locationToUpdate != null) {
            Location location = LocationMapper.toLocation(locationToUpdate);
            Location newLocation = addLocation(location);
            event.setLocationId(newLocation.getId());
        }
        if (paidToUpdate != null) {
            event.setPaid(paidToUpdate);
        }
        if (participantLimitToUpdate != null) {
            validatePositive(participantLimitToUpdate, "максимальное количество участников");
            event.setParticipantLimit(participantLimitToUpdate);
        }
        if (requestModerationToUpdate != null) {
            event.setRequestModeration(requestModerationToUpdate);
        }
        if (titleToUpdate != null) {
            validateStringField(titleToUpdate, "заголовок", 3, 120);
            event.setTitle(titleToUpdate);
        }
    }

    private EventDtoOut eventFullDto(Event event) {
        Location location = locationRepo.find(event.getLocationId());
        User user = userRepo.find(event.getInitiator());
        Category category = categoryRepo.find(event.getCategoryId());
        int confirmedRequests = requestRepo.countConfirmedRequestsByEventId(event.getId());
        int views = 0;
        if (event.getEventState() == PUBLISHED) {
            Collection<ViewStatsDto> viewStatsDto = statsIntegration.statsRequest((List.of(event)));
            if (!viewStatsDto.isEmpty()) {
                long eventId = getEventId(new ArrayList<>(viewStatsDto).get(0));
                if (event.getId() != eventId) {
                    throw new StatsRequestException(
                            String.format("Ошибка запроса статистики: запрошенный id %d не совпадает с возвращенным %d",
                                    event.getId(), eventId)
                    );
                }
            }
            views = viewStatsDto.isEmpty() ? 0 : (int) new ArrayList<>(viewStatsDto).get(0).getHits();
        }
        return EventMapper.toEventDto(event, category, user, location, confirmedRequests, views);
    }

    private Collection<EventDtoOut> eventsFullDtoOut(Collection<Event> events) {
        return events.stream()
                .map(i -> {
                    int confirmedRequests = requestRepo.countConfirmedRequestsByEventId(i.getId());
                    int views = 0;
                    if (i.getEventState() == PUBLISHED) {
                        Collection<ViewStatsDto> viewStatsDto = statsIntegration.statsRequest((List.of(i)));
                        if (!viewStatsDto.isEmpty()) {
                            long eventId = getEventId(new ArrayList<>(viewStatsDto).get(0));
                            if (i.getId() != eventId) {
                                throw new StatsRequestException(
                                        String.format("Ошибка запроса статистики: запрошенный id %d не совпадает с возвращенным %d",
                                                i.getId(), eventId)
                                );
                            }
                        }
                        views = viewStatsDto.isEmpty() ? 0 : (int) new ArrayList<>(viewStatsDto).get(0).getHits();
                    }
                    User user = userRepo.find(i.getInitiator());
                    Category category = categoryRepo.find(i.getCategoryId());
                    Location location = locationRepo.find(i.getLocationId());
                    return EventMapper.toEventDto(i, category, user, location, confirmedRequests, views);
                })
                .collect(Collectors.toList());
    }

    private Collection<EventDtoOut> eventsFullSortedByViewsDtoList(Collection<Event> events, int from, int size) {
        if (events.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<ViewStatsDto> viewsStatsDto = statsIntegration.statsRequest(events);
        Map<Long, Integer> viewsByEventId = viewsMap(viewsStatsDto, events);
        viewsByEventId = viewsByEventId.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .skip(from)
                .limit(size)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<Long, Event> eventsByIds = events.stream().collect(Collectors.toMap(Event::getId, Function.identity()));

        events = viewsByEventId.keySet().stream()
                .map(eventsByIds::get)
                .collect(Collectors.toList());

        Collection<Category> categories = findCategoriesByEvents(events);
        Collection<User> users = findUsersByEvents(events);
        Collection<Location> locations = findLocationsByEvents(events);
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Integer> confirmedRequestsByEventId = requestRepo.countConfirmedRequestsByEventIds(eventIds);

        return EventMapper.toEventDtoList(events, categories, users, locations,
                confirmedRequestsByEventId, viewsByEventId);
    }

    private Collection<EventDtoOut> eventsFullDtoOutList(Collection<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());

        Collection<Category> categories = findCategoriesByEvents(events);
        Collection<User> users = findUsersByEvents(events);
        Collection<Location> locations = findLocationsByEvents(events);
        Map<Long, Integer> confirmedRequestsByEventId = requestRepo.countConfirmedRequestsByEventIds(eventIds);

        Collection<ViewStatsDto> viewsStatsDto = statsIntegration.statsRequest(events);

        return EventMapper.toEventDtoList(events, categories, users, locations,
                confirmedRequestsByEventId, viewsStatsDto);
    }

    private static Map<Long, Integer> viewsMap(Collection<ViewStatsDto> viewStatsDtos, Collection<Event> events) {
        Map<Long, Integer> viewsByEventId = events.stream()
                .collect(Collectors.toMap(Event::getId, event -> 0));
        if (viewStatsDtos.isEmpty()) {
            return viewsByEventId;
        }
        viewStatsDtos.forEach(viewStatsDto -> viewsByEventId.put(getEventId(viewStatsDto), (int) viewStatsDto.getHits()));
        return viewsByEventId;
    }

    private static long getEventId(ViewStatsDto viewStatsDto) {
        try {
            return Long.parseLong(viewStatsDto.getUri().substring(8, viewStatsDto.getUri().length())); //Long.parseLong(tokenizer.nextToken());
        } catch (NumberFormatException e) {
            throw new StatsRequestException("Ошибка запроса данных статистики");
        }
    }

    private Collection<Location> findLocationsByEvents(Collection<Event> events) {
        Collection<Long> locationIds = events.stream().map(Event::getLocationId).distinct().collect(Collectors.toList());
        return locationRepo.findByIds(locationIds);
    }

    private Collection<User> findUsersByEvents(Collection<Event> events) {
        Collection<Long> userIds = events.stream().map(Event::getInitiator).distinct().collect(Collectors.toList());
        return userRepo.findByIds(userIds);
    }

    private Collection<Category> findCategoriesByEvents(Collection<Event> events) {
        Collection<Long> categoryIds = events.stream().map(Event::getCategoryId).distinct().collect(Collectors.toList());
        return categoryRepo.findByIds(categoryIds);
    }

    private void checkUser(Event event, long userId) {
        if (event.getInitiator() != userId) {
            throw new NotFoundException(
                    String.format("Пользователь с id %d не организатор события с id %d", userId, event.getId()));
        }
    }

    private void setEventStateByPrivateAction(Event event, String stateAction) {
        EventStatePrivate action = EventStatePrivate.getState(stateAction);
        if (action == EventStatePrivate.SEND_TO_REVIEW) {
            event.setEventState(EventState.PENDING);
        } else {
            event.setEventState(EventState.CANCELED);
        }
    }

    private void validateEventDate(LocalDateTime eventDateToUpdate) {
        if (!eventDateToUpdate.isAfter(LocalDateTime.now())) {
            throw new ValidationException("Поле eventDate должно содержать дату, которая еще не наступила");
        }
    }

    private void validatePositive(int field, String fieldName) {
        if (field < 0) {
            throw new ValidationException(String.format("Поле %s не может быть отрицательным", fieldName));
        }
    }

    private void validateStringField(String field, String fieldName, int min, int max) {
        if (field.isBlank()) {
            throw new ValidationException(String.format("Поле %s не может быть пустым", fieldName));
        }
        if (field.length() < min || field.length() > max) {
            throw new ValidationException(String.format("Длина поля %s должна быть от %d до %d", fieldName, min, max));
        }
    }
}

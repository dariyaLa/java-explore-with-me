package ru.practicum.events.dto;

import ru.practicum.StatsRequestException;
import ru.practicum.ViewStatsDto;
import ru.practicum.categories.Category;
import ru.practicum.categories.CategoryMapper;
import ru.practicum.events.Event;
import ru.practicum.events.enums.EventState;
import ru.practicum.location.Location;
import ru.practicum.location.LocationMapper;
import ru.practicum.users.User;
import ru.practicum.users.UserMapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.constants.Constant.getZoneOffset;

public class EventMapper {

    public static Event toEvent(EventDto eventDto, long locationId, long userId) {
        return Event.builder()
                .annotation(eventDto.getAnnotation())
                .categoryId(eventDto.getCategory())
                .description(eventDto.getDescription())
                .eventDate(eventDto.getEventDate().toInstant(getZoneOffset()))
                .locationId(locationId)
                .paid(eventDto.isPaid())
                .participantLimit(eventDto.getParticipantLimit())
                .requestModeration(eventDto.getRequestModeration())
                .title(eventDto.getTitle())
                .initiator(userId)
                .eventState(EventState.PENDING)
                .build();
    }

    public static EventDtoOut toEventDto(Event event,
                                         Category category,
                                         User user,
                                         Location location,
                                         int confirmedRequests,
                                         int views) {
        EventDtoOut eventDtoOut = EventDtoOut.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDtoOut(category))
                .confirmedRequests(confirmedRequests)
                .createdOn(LocalDateTime.ofInstant(event.getCreatedOn(), getZoneOffset()))
                .description(event.getDescription())
                .eventDate(LocalDateTime.ofInstant(event.getEventDate(), getZoneOffset()))
                .initiator(UserMapper.toUserDto(user))
                .location(LocationMapper.toLocationDto(location))
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.isRequestModeration())
                .title(event.getTitle())
                .views(views)
                .state(event.getEventState())
                .build();
        if (event.getPublishedOn() != null) {
            eventDtoOut.setPublishedOn(LocalDateTime.ofInstant(event.getPublishedOn(), getZoneOffset()));
        }
        return eventDtoOut;
    }

    public static Collection<EventDtoOut> toEventDtoList(Collection<Event> events,
                                                         Collection<Category> categories,
                                                         Collection<User> users,
                                                         Collection<Location> locations,
                                                         Map<Long, Integer> confirmedRequestsByEventId,
                                                         Map<Long, Integer> viewsByEventId) {
        Map<Long, Category> categoriesById = categoryMap(categories);
        Map<Long, User> usersById = usersMap(users);
        Map<Long, Location> locationsById = locationsMap(locations);

        return events.stream().map(event -> {
            long eventId = event.getId();
            return toEventDto(event,
                    categoriesById.get(event.getCategoryId()),
                    usersById.get(event.getInitiator()),
                    locationsById.get(event.getLocationId()),
                    confirmedRequestsByEventId.get(eventId),
                    viewsByEventId.get(eventId)
            );
        }).collect(Collectors.toList());

    }

    public static Collection<EventDtoOut> toEventDtoList(Collection<Event> events,
                                                         Collection<Category> categories,
                                                         Collection<User> users,
                                                         Collection<Location> locations,
                                                         Map<Long, Integer> confirmedRequestsByEventId,
                                                         Collection<ViewStatsDto> viewStatsDtos) {

        Map<Long, Integer> viewsByEventId = viewMap(viewStatsDtos, events);
        return toEventDtoList(events, categories, users, locations, confirmedRequestsByEventId, viewsByEventId);
    }

    public static Collection<EventShortDtoOut> toEventShortDto(Collection<Event> events,
                                                               Collection<Category> categories,
                                                               Collection<User> users,
                                                               Map<Long, Integer> confirmedRequestsByEventId,
                                                               Collection<ViewStatsDto> viewStatsDtos) {
        Map<Long, Category> categoriesById = categoryMap(categories);
        Map<Long, User> usersById = usersMap(users);
        Map<Long, Integer> viewsByEventId = viewMap(viewStatsDtos, events);

        return events.stream().map(event -> {
            long eventId = event.getId();
            return toEventShortDto(event,
                    categoriesById.get(event.getCategoryId()),
                    confirmedRequestsByEventId.get(eventId),
                    usersById.get(event.getInitiator()),
                    viewsByEventId.get(eventId)
            );
        }).collect(Collectors.toList());
    }

    public static EventShortDtoOut toEventShortDto(Event event,
                                                   Category category,
                                                   int confirmedRequests,
                                                   User user,
                                                   long views) {
        return EventShortDtoOut.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(category))
                .confirmedRequests(confirmedRequests)
                .eventDate(LocalDateTime.ofInstant(event.getEventDate(), getZoneOffset()))
                .initiator(UserMapper.toUserDto(user))
                .paid(event.isPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    private static Map<Long, User> usersMap(Collection<User> users) {
        return users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private static Map<Long, Category> categoryMap(Collection<Category> categories) {
        return categories.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
    }

    private static Map<Long, Location> locationsMap(Collection<Location> locations) {
        return locations.stream()
                .collect(Collectors.toMap(Location::getId, Function.identity()));
    }

    public static Map<Long, Integer> viewMap(Collection<ViewStatsDto> viewStatsDtos, Collection<Event> events) {
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
//        StringTokenizer tokenizer = new StringTokenizer(viewStatsDto.getUri(), "/");
//        if (!tokenizer.nextToken().equals(URI_EVENTS)) {
//            throw new StatsRequestException("Ошибка запроса статистики");
//        }
//        try {
//            return Long.parseLong(tokenizer.nextToken());
//        } catch (NumberFormatException e) {
//            throw new StatsRequestException("Ошибка запроса данных статистики");
//        }
//    }
}

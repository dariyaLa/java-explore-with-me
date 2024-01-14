package ru.practicum.events;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.RepositoryMain;
import ru.practicum.events.enums.EventSort;
import ru.practicum.events.enums.EventState;
import ru.practicum.exception.NotFoundException;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.constants.Constant.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventRepoImpl implements RepositoryMain<Event, Event> {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public Event add(Event obj) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("events")
                .usingGeneratedKeyColumns("id", "created_on");
        KeyHolder generatedKeys = simpleJdbcInsert.executeAndReturnKeyHolder(obj.toMap());
        Map<String, Object> keys = generatedKeys.getKeys();
        long id = (long) Objects.requireNonNull(keys).get("id");
        Timestamp createdOn = (Timestamp) keys.get("created_on");
        obj.setId(id);
        obj.setCreatedOn(createdOn.toInstant());
        return obj;
    }

    @Override
    public Event update(Event event, Long id) {
        String sql = "update events set annotation = :annotation, " +
                "category_id = :categoryId, " +
                "description = :description, " +
                "event_date = :eventDate, " +
                "location_id = :locationId, " +
                "paid = :paid, " +
                "participant_limit = :participantLimit, " +
                "request_moderation = :requestModeration, " +
                "title = :title, " +
                "initiator = :initiator, " +
                "created_on = :createdOn ";
        MapSqlParameterSource parameters = makeParameterMap(event);
        if (event.getEventState() == EventState.PUBLISHED) {
            sql = sql + ", published_on = :publishedOn , state = 'PUBLISHED' ";
        } else if (event.getEventState() == EventState.CANCELED) {
            sql = sql + ", state = 'CANCELED' ";
        }
        sql = sql + "where id = :id";
        if (namedJdbcTemplate.update(sql, parameters) > 0) {
            return event;
        }
        log.warn("Событие с id {} не найдено", event.getId());
        throw new NotFoundException(String.format("Событие с id %d не найдено", event.getId()));
    }

    public Event update(Event event) {
        String sql = "update events set annotation = :annotation, " +
                "category_id = :categoryId, " +
                "description = :description, " +
                "event_date = :eventDate, " +
                "location_id = :locationId, " +
                "paid = :paid, " +
                "participant_limit = :participantLimit, " +
                "request_moderation = :requestModeration, " +
                "title = :title, " +
                "initiator = :initiator, " +
                "created_on = :createdOn ";
        MapSqlParameterSource parameters = makeParameterMap(event);
        if (event.getEventState() == EventState.PUBLISHED) {
            sql = sql + ", published_on = :publishedOn , state = 'PUBLISHED' ";
        } else if (event.getEventState() == EventState.CANCELED) {
            sql = sql + ", state = 'CANCELED' ";
        }
        sql = sql + "where id = :id";
        if (namedJdbcTemplate.update(sql, parameters) > 0) {
            return event;
        }
        log.warn("Событие с id {} не найдено", event.getId());
        throw new NotFoundException(String.format("Событие с id %d не найдено", event.getId()));
    }

    @Override
    public Event find(Long id) {
        String sql = "select * from events where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToEvent(rs), id);
        } catch (DataRetrievalFailureException e) {
            log.warn("Событие с id {} не найдено", id);
            throw new NotFoundException(String.format("Событие с id %d не найдено", id));
        }
    }

    public Collection<Event> findByIds(Collection<Long> ids) {
        String sql = "select * from events where id in (:ids)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToEvent(rs));
    }

    @Override
    public Collection<Event> findAll(Integer from, Integer size) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    public long findEventsByCategoryId(long catId) {
        String sql = "select COUNT(id) as countEventsByCat " +
                "from events where category_id = :catId";
        MapSqlParameterSource parameters = new MapSqlParameterSource("catId", catId);
        Long countEvents = namedJdbcTemplate.queryForObject(sql, parameters, Long.class);
        return countEvents == null ? 0 : countEvents;
    }

    public Collection<Event> findAllWithFilter(Map<String, Object> filter) {
        StringBuilder sql = new StringBuilder("select * from events");
        Collection<String> conditions = new ArrayList<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        if (filter.get(IS_PUBLISHED) != null) {
            conditions.add("state = 'PUBLISHED'");
        }

        if (filter.get(TEXT) != null) {
            Collection<String> textParams = List.of("annotation", "description", "title");
            String textConditions = textParams.stream()
                    .map(param -> "lower(" + param + ") like concat('%', :text, '%')")
                    .collect(Collectors.joining(" OR ", "(", ")"));
            conditions.add(textConditions);
            parameters.addValue("text", filter.get(TEXT));
        }

        if (filter.get(PAID) != null) {
            conditions.add("paid = :paid");
            parameters.addValue("paid", filter.get(PAID));
        }

        if (filter.get(START) != null) {
            conditions.add("event_date >= :start");
            parameters.addValue("start", Timestamp.from((Instant) filter.get(START)));
        }

        if (filter.get(END) != null) {
            conditions.add("(event_date <= :end)");
            parameters.addValue("end", Timestamp.from((Instant) filter.get(END)));
        }

        if (filter.get(USERS) != null) {
            conditions.add("initiator in (:initiators)");
            parameters.addValue("initiators", filter.get(USERS));
        }

        if (filter.get(STATES) != null) {
            conditions.add("state in (:states)");
            parameters.addValue("states", filter.get(STATES));
        }

        if (!conditions.isEmpty()) {
            String allConditions = conditions.stream()
                    .collect(Collectors.joining(" AND ", "(", ")"));
            sql.append(" where ").append(allConditions);
        }

        if (filter.get(SORT) != null && filter.get(SORT) == EventSort.EVENT_DATE) {
            sql.append(" order by event_date");
        }
        if (filter.get(SORT) == null || filter.get(SORT) != EventSort.VIEWS) {
            sql.append(" limit :size offset :from");
            parameters.addValue("size", filter.get(SIZE));
            parameters.addValue("from", filter.get(FROM));
        }

        return namedJdbcTemplate.query(sql.toString(), parameters, (rs, rowNum) -> mapRowToEvent(rs));
    }

    public List<Event> findAllByUser(Map<String, Object> filter) {
        String sql = "select * from events where initiator = :userId " +
                "order by id limit :size offset :from";
        MapSqlParameterSource parameters = new MapSqlParameterSource("userId", filter.get(USER));
        parameters.addValue("from", filter.get(FROM));
        parameters.addValue("size", filter.get(SIZE));
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToEvent(rs));
    }

    @SneakyThrows
    private Event mapRowToEvent(ResultSet rs) {
        long id = rs.getLong("id");
        String annotation = rs.getString("annotation");
        long categoryId = rs.getLong("category_id");
        String description = rs.getString("description");
        Instant eventDate = rs.getTimestamp("event_date").toInstant();
        long locationId = rs.getLong("location_id");
        boolean paid = rs.getBoolean("paid");
        int participantLimit = rs.getInt("participant_limit");
        boolean requestModeration = rs.getBoolean("request_moderation");
        String title = rs.getString("title");
        long initiator = rs.getLong("initiator");
        Instant createdOn = rs.getTimestamp("created_on").toInstant();
        EventState eventState = EventState.valueOf(rs.getString("state"));

        Event event = Event.builder()
                .id(id)
                .annotation(annotation)
                .categoryId(categoryId)
                .description(description)
                .eventDate(eventDate)
                .locationId(locationId)
                .paid(paid)
                .participantLimit(participantLimit)
                .requestModeration(requestModeration)
                .title(title)
                .initiator(initiator)
                .createdOn(createdOn)
                .eventState(eventState)
                .build();

        if (eventState == EventState.PUBLISHED) {
            Instant publishedOn = rs.getTimestamp("published_on").toInstant();
            event.setPublishedOn(publishedOn);
        }
        return event;
    }

    private MapSqlParameterSource makeParameterMap(Event event) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("id", event.getId());
        parameters.addValue("annotation", event.getAnnotation());
        parameters.addValue("categoryId", event.getCategoryId());
        parameters.addValue("description", event.getDescription());
        parameters.addValue("eventDate", Timestamp.from(event.getEventDate()));
        parameters.addValue("locationId", event.getLocationId());
        parameters.addValue("paid", event.isPaid());
        parameters.addValue("participantLimit", event.getParticipantLimit());
        parameters.addValue("requestModeration", event.isRequestModeration());
        parameters.addValue("title", event.getTitle());
        parameters.addValue("initiator", event.getInitiator());
        parameters.addValue("createdOn", Timestamp.from(event.getCreatedOn()));
        Timestamp publishedOnTimestamp = event.getPublishedOn() == null ? null :
                Timestamp.from(event.getPublishedOn());
        parameters.addValue("publishedOn", publishedOnTimestamp);
        parameters.addValue("eventState", event.getEventState());
        return parameters;
    }
}

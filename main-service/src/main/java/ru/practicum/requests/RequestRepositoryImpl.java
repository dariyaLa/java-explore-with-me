package ru.practicum.requests;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.RepositoryMain;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class RequestRepositoryImpl implements RepositoryMain<Request, Request> {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public Request add(Request request) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("requests")
                .usingGeneratedKeyColumns("id", "created");
        KeyHolder generatedKeys = simpleJdbcInsert.executeAndReturnKeyHolder(request.toMap());
        Map<String, Object> keys = generatedKeys.getKeys();
        long id = (long) Objects.requireNonNull(keys).get("id");
        Timestamp created = (Timestamp) keys.get("created");
        request.setId(id);
        request.setCreated(created.toInstant());
        return request;
    }

    public int countConfirmedRequestsByEventId(long eventId) {
        String sql = "select COUNT(id) as requests_qty from requests " +
                "where status = 'CONFIRMED' and event_id = :eventId";
        MapSqlParameterSource parameters = new MapSqlParameterSource("eventId", eventId);
        Integer confirmedRequestsByEventIds = namedJdbcTemplate.queryForObject(sql, parameters, Integer.class);
        return confirmedRequestsByEventIds != null ? confirmedRequestsByEventIds : 0;
    }

    public int countRequestsByEventId(long eventId) {
        String sql = "select COUNT(id) as requests_qty from requests where event_id = :eventId";
        MapSqlParameterSource parameters = new MapSqlParameterSource("eventId", eventId);
        Integer confirmedRequestsByEventIds = namedJdbcTemplate.queryForObject(sql, parameters, Integer.class);
        return confirmedRequestsByEventIds != null ? confirmedRequestsByEventIds : 0;
    }

    public Map<Long, Integer> countConfirmedRequestsByEventIds(Collection<Long> eventIds) {
        String sql = "select event_id, COUNT(id) as requests_qty from requests " +
                "where status = 'CONFIRMED' group by event_id";
        Map<Long, Integer> confirmedRequestsByEventIds = eventIds.stream()
                .collect(Collectors.toMap(Function.identity(), eventId -> 0));
        MapSqlParameterSource parameters = new MapSqlParameterSource("eventIds", eventIds);
        namedJdbcTemplate.query(sql, parameters,
                rs -> {
                    long eventId = rs.getLong("event_id");
                    int requestQty = rs.getInt("requests_qty");
                    confirmedRequestsByEventIds.put(eventId, requestQty);
                });
        return confirmedRequestsByEventIds;
    }

    @Override
    public Request update(Request obj, Long id) {
        throw new ConflictException(String.format("Данная операция не поддерживается. Допустимо создать новый запрос, " +
                "закрыть старый"));
    }

    @Override
    public Request find(Long id) {
        String sql = "select * from requests where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToRequest(rs), id);
        } catch (DataRetrievalFailureException e) {
            throw new NotFoundException(String.format("Запрос с id %d не найден", id));
        }
    }

    @Override
    public Collection<Request> findAll(Integer from, Integer size) {
        String sql = "select * from requests order by id limit :size offset :from";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("from", from);
        parameters.addValue("size", size);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToRequest(rs));
    }

    public Collection<Request> findAllByUser(Long userId) {
        String sql = "select * from requests where requester_id = :requesterId";
        MapSqlParameterSource parameters = new MapSqlParameterSource("requesterId", userId);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToRequest(rs));
    }

    @Override
    public void delete(Long id) {
        String sql = "delete from requests where id = ?";
        if (jdbcTemplate.update(sql, id) < 0) {
            throw new NotFoundException(String.format("Запрос с id %d не найден", id));
        }

    }

    public void cancel(long requestId) {
        String sql = "update requests set status = 'CANCELED' where id = :requestId";
        MapSqlParameterSource parameters = new MapSqlParameterSource("requestId", requestId);
        namedJdbcTemplate.update(sql, parameters);
    }

    public Collection<Request> findByIds(Collection<Long> requestIds) {
        String sql = "select * from requests where id in (:requestIds)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("requestIds", requestIds);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToRequest(rs));
    }

    public void updateStates(Collection<Long> requestIds, RequestState requestState) {
        String sql = "update requests set status = :status where id in (:requestIds)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("requestIds", requestIds);
        parameters.addValue("status", requestState.toString());
        namedJdbcTemplate.update(sql, parameters);
    }

    public Collection<Request> findByEventId(long eventId) {
        String sql = "select * from requests where event_id = :eventId";
        MapSqlParameterSource parameters = new MapSqlParameterSource("eventId", eventId);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToRequest(rs));
    }

    @SneakyThrows
    private Request mapRowToRequest(ResultSet rs) {
        long id = rs.getLong("id");
        long eventId = rs.getLong("event_id");
        long requesterId = rs.getLong("requester_id");
        Instant created = rs.getTimestamp("created").toInstant();
        RequestState requestState = RequestState.getState(rs.getString("status"));

        return Request.builder()
                .id(id)
                .eventId(eventId)
                .requesterId(requesterId)
                .created(created)
                .status(requestState)
                .build();
    }
}

package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.practicum.interfaces.HitRepository;
import ru.practicum.model.Hit;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
@RequiredArgsConstructor
public class HitRepositoryImpl implements HitRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Hit addHit(Hit hit) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("hits")
                .usingGeneratedKeyColumns("id");
        long id = simpleJdbcInsert.executeAndReturnKey(hit.toMap()).longValue();
        hit.setId(id);
        return hit;
    }

    @Override
    public Map<Long, Long> getHitsById(Instant start, Instant end) {
        String sql = "select app_id, COUNT(ip) as views from hits " +
                "where (timestamp between :start and :end) " +
                "group by app_id";
        MapSqlParameterSource parameters = getParamsWithDates(start, end);
        return query(sql, parameters);
    }

    @Override
    public Map<Long, Long> getHitsById(Instant start, Instant end, List<Long> appIds) {
        String sql = "select app_id, COUNT(ip) as views from hits " +
                "where (timestamp between :start and :end) " +
                "AND app_id in (:appIds) " +
                "group by app_id";
        MapSqlParameterSource parameters = getParamsWithDates(start, end);
        parameters.addValue("appIds", appIds);
        return query(sql, parameters);
    }

    @Override
    public Map<Long, Long> getUniqueHitsByAppId(Instant start, Instant end) {
        String sql = "select app_id, COUNT(DISTINCT(ip)) as views from hits " +
                "where (timestamp between :start and :end) " +
                "group by app_id";
        MapSqlParameterSource parameters = getParamsWithDates(start, end);
        return query(sql, parameters);
    }

    @Override
    public Map<Long, Long> getUniqueHitsByAppId(Instant start, Instant end, List<Long> appIds) {
        String sql = "select app_id, COUNT(DISTINCT(ip)) as views from hits " +
                "where (timestamp between :start and :end) " +
                "AND app_id in (:appIds) " +
                "group by app_id";
        MapSqlParameterSource parameters = getParamsWithDates(start, end);
        parameters.addValue("appIds", appIds);
        return query(sql, parameters);
    }

    private Map<Long, Long> query(String sql, MapSqlParameterSource parameters) {
        final Map<Long, Long> hitsByAppId = new HashMap<>();
        namedParameterJdbcTemplate.query(sql, parameters,
                rs -> {
                    long appId = rs.getLong("app_id");
                    long views = rs.getLong("views");
                    hitsByAppId.put(appId, views);
                });
        return hitsByAppId;
    }

    private MapSqlParameterSource getParamsWithDates(Instant start, Instant end) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("start", Timestamp.from(start));
        parameters.addValue("end", Timestamp.from(end));
        return parameters;
    }
}

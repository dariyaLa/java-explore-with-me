package ru.practicum.location;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.practicum.RepositoryMain;
import ru.practicum.exception.NotFoundException;

import java.sql.ResultSet;
import java.util.Collection;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LocationRepoImpl implements RepositoryMain<Location, Location> {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public Location add(Location obj) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("locations")
                .usingGeneratedKeyColumns("id");
        Long id = simpleJdbcInsert.executeAndReturnKey(obj.toMap()).longValue();
        obj.setId(id);
        return obj;
    }

    @Override
    public Location update(Location location, Long id) {
        String sql = "update events set " +
                "lat = :lat, " +
                "lon = :lon where id= :id ";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("id", id);
        parameters.addValue("lat", location.getLat());
        parameters.addValue("lon", location.getLon());

        if (namedJdbcTemplate.update(sql, parameters) > 0) {
            return location;
        }
        throw new NotFoundException(String.format("Локация с id %d не найдено", location.getId()));
    }

    @Override
    public Location find(Long id) {
        String sql = "select * from locations where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToLocation(rs), id);
        } catch (DataRetrievalFailureException e) {
            log.warn("Локация с id {} не найдена", id);
            throw new NotFoundException(String.format("Локация с id %d не найдена", id));
        }
    }

    @Override
    public Collection<Location> findAll(Integer from, Integer size) {
        String sql = "select * from locations order by id limit :size offset :from";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("from", from);
        parameters.addValue("size", size);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToLocation(rs));
    }

    @Override
    public void delete(Long id) {
        String sql = "delete from locations where id = ?";
        if (jdbcTemplate.update(sql, id) < 0) {
            log.warn("Локация с id {} не найдена", id);
            throw new NotFoundException(String.format("Локация с id %d не найдена", id));
        }

    }

    public Collection<Location> findByIds(Collection<Long> ids) {
        String sql = "select * from locations where id in (:ids)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToLocation(rs));
    }

    @SneakyThrows
    private Location mapRowToLocation(ResultSet rs) {
        long id = rs.getLong("id");
        float lat = rs.getFloat("lat");
        float lon = rs.getFloat("lon");

        return Location.builder()
                .id(id)
                .lat(lat)
                .lon(lon)
                .build();
    }

}

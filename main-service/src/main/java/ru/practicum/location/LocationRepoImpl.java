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
    public Location update(Location obj, Long id) {
        return null;
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
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    public Collection<Location> findNearestByLatAndLon(Location location) {
        String sql = "select * from locations where distance(:lat, :lon, lat, lon) <= radius " +
                "order by radius";
        MapSqlParameterSource parameters = new MapSqlParameterSource("lat", location.getLat());
        parameters.addValue("lon", location.getLon());
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToLocation(rs));
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

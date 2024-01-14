package ru.practicum.compilations;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CompilationRepoImpl implements RepositoryMain<Compilation, Compilation> {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public Compilation add(Compilation compilation) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("compilations")
                .usingGeneratedKeyColumns("id");
        long id = simpleJdbcInsert.executeAndReturnKey(compilation.toMap()).longValue();
        compilation.setId(id);
        return compilation;
    }

    public void addEventsByCompId(long compId, Collection<Long> eventIds) {
        StringBuilder sql = new StringBuilder("insert into compilations_events(compilation_id, event_id) values ");
        MapSqlParameterSource parameters = new MapSqlParameterSource("compId", compId);
        String values = IntStream.range(0, eventIds.size())
                //
                .peek(i -> parameters.addValue("eventId" + i, new ArrayList<>(eventIds).get(i)))
                .mapToObj(i -> "(:compId, :eventId" + i + ")")
                .collect(Collectors.joining(", "));
        sql.append(values);
        namedJdbcTemplate.update(sql.toString(), parameters);
    }

    @Override
    public Compilation update(Compilation compilation, Long id) {
        String sql = "update compilations set pinned = :pinned, title = :title where id = :id";
        MapSqlParameterSource parameters = new MapSqlParameterSource("id", compilation.getId());
        parameters.addValue("pinned", compilation.isPinned());
        parameters.addValue("title", compilation.getTitle());
        if (namedJdbcTemplate.update(sql, parameters) < 0) {
            throw new NotFoundException(String.format("Подборка с id %d не найдена", id));
        }
        return compilation;
    }

    public Compilation update(Compilation compilation) {
        String sql = "update compilations set pinned = :pinned, title = :title where id = :id";
        MapSqlParameterSource parameters = new MapSqlParameterSource("id", compilation.getId());
        parameters.addValue("pinned", compilation.isPinned());
        parameters.addValue("title", compilation.getTitle());
        if (namedJdbcTemplate.update(sql, parameters) < 0) {
            throw new NotFoundException(String.format("Подборка с id %d не найдена", compilation.getId()));
        }
        return compilation;
    }

    @Override
    public Compilation find(Long id) {
        String sql = "select * from compilations where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToCompilation(rs), id);
        } catch (DataRetrievalFailureException e) {
            log.warn("Подборка с id {} не найдена", id);
            throw new NotFoundException(String.format("Подборка с id %d не найдена", id));
        }
    }

    @Override
    public Collection<Compilation> findAll(Integer from, Integer size) {
        String sql = "select * from compilations order by id limit :size offset :from";
        MapSqlParameterSource parameters = new MapSqlParameterSource("from", from);
        parameters.addValue("size", size);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToCompilation(rs));
    }

    public Collection<Long> findEventIdsByCompId(long compId) {
        String sql = "select event_id from compilations_events where compilation_id = :compId";
        MapSqlParameterSource parameters = new MapSqlParameterSource("compId", compId);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> rs.getLong("event_id"));
    }

    public Map<Long, Collection<Long>> findEventIdsByCompIds(Collection<Long> compIds) {
        String sql = "select compilation_id, event_id from compilations_events where compilation_id in (:compIds)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("compIds", compIds);
        final Map<Long, Collection<Long>> eventIdsByCompIds = compIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> new ArrayList<>()));

        namedJdbcTemplate.query(sql, parameters,
                rs -> {
                    long compId = rs.getLong("compilation_id");
                    long eventId = rs.getLong("event_id");
                    eventIdsByCompIds.get(compId).add(eventId);
                });
        return eventIdsByCompIds;
    }

    @Override
    public void delete(Long id) {
        String sql = "delete from compilations where id = ?";
        if (jdbcTemplate.update(sql, id) <= 0) {
            throw new NotFoundException(String.format("Подборка с id %d не найдена", id));
        }

    }

    public List<Compilation> findAllWithFilter(boolean pinned, int from, int size) {
        String sql = "select * from compilations where pinned = :pinned " +
                "order by id limit :size offset :from";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("pinned", pinned);
        parameters.addValue("from", from);
        parameters.addValue("size", size);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToCompilation(rs));
    }

    public void removeEventsByCompId(long compId) {
        String sql = "delete from compilations_events where compilation_id = ?";
        jdbcTemplate.update(sql, compId);
    }

    @SneakyThrows
    private Compilation mapRowToCompilation(ResultSet rs) {
        long id = rs.getLong("id");
        String title = rs.getString("title");
        boolean pinned = rs.getBoolean("pinned");

        return Compilation.builder()
                .id(id)
                .title(title)
                .pinned(pinned)
                .build();
    }
}

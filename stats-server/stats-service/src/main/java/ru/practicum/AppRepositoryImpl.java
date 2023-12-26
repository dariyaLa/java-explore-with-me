package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.practicum.interfaces.AppRepository;
import ru.practicum.model.App;

import java.sql.ResultSet;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AppRepositoryImpl implements AppRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate parameterJdbcTemplate;

    @Override
    public List<App> findByAppAndUri(String app, String uri) {
        String sql = "select id, name, uri from apps where name = :app and uri = :uri";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("app", app);
        parameters.addValue("uri", uri);
        return parameterJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToApp(rs));
    }

    @Override
    public long add(App app) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("apps")
                .usingGeneratedKeyColumns("id");
        return simpleJdbcInsert.executeAndReturnKey(app.toMap()).longValue();
    }

    @Override
    public List<App> getAppsByUris(List<String> uris) {
        String sql = "select id, name, uri from apps where uri in (:uris)";
        SqlParameterSource parameters = new MapSqlParameterSource("uris", uris);
        return parameterJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToApp(rs));

    }

    @Override
    public List<App> getAppsByIds(List<Long> ids) {
        String sql = "select id, name, uri from apps a where id in (:ids)";
        SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        return parameterJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToApp(rs));
    }

    @SneakyThrows
    private App mapRowToApp(ResultSet rs) {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String uri = rs.getString("uri");
        return App.builder()
                .id(id)
                .name(name)
                .uri(uri)
                .build();
    }
}

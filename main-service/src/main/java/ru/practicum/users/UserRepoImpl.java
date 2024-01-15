package ru.practicum.users;

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
public class UserRepoImpl implements RepositoryMain<User, UserDtoOut> {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public User add(User obj) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
        long id = simpleJdbcInsert.executeAndReturnKey(obj.toMap()).longValue();
        obj.setId(id);
        return obj;
    }

    @Override
    public User update(User user, Long id) {
        find(id);
        String sql = "update events set " +
                "name = :name, " +
                "email = :email where id= :id ";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("id", id);
        parameters.addValue("name", user.getName());
        parameters.addValue("email", user.getEmail());

        if (namedJdbcTemplate.update(sql, parameters) > 0) {
            return user;
        }
        throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
    }

    @Override
    public User find(Long id) {
        String sql = "select * from users where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToUser(rs), id);
        } catch (DataRetrievalFailureException e) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }
    }

    @Override
    public Collection<User> findAll(Integer from, Integer size) {
        String sql = "select * from users order by id limit :size offset :from";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("from", from);
        parameters.addValue("size", size);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToUser(rs));
    }

    @Override
    public void delete(Long id) {
        String sql = "delete from users where id = ?";
        if (jdbcTemplate.update(sql, id) < 0) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }
    }

    public Collection<User> findAllIds(Collection<Long> ids, int from, int size) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("from", from);
        parameters.addValue("size", size);
        if (ids.isEmpty()) {
            String sql = "select * from users order by id limit :size offset :from";
            return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToUser(rs));

        } else {
            String sql = "select * from users where id in (:ids) order by id limit :size offset :from";
            parameters.addValue("ids", ids);
            return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToUser(rs));
        }
    }

    public Collection<User> findByIds(Collection<Long> ids) {
        String sql = "select * from users where id in (:ids)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToUser(rs));
    }

    @SneakyThrows
    private User mapRowToUser(ResultSet rs) {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }
}

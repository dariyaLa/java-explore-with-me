package ru.practicum.categories;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.practicum.RepositoryMain;
import ru.practicum.exception.NotFoundException;

import java.sql.ResultSet;
import java.util.Collection;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CategoryRepoImpl implements RepositoryMain<Category, CategoryDtoOut> {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public Category add(Category obj) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("categories")
                .usingGeneratedKeyColumns("id");
        long id = simpleJdbcInsert.executeAndReturnKey(obj.toMap()).longValue();
        obj.setId(id);
        return obj;
    }

    @Override
    public Category update(Category obj, Long id) {
        find(id);
        obj.setId(id);
        String sql = "update categories set name = ? where id = ?";
        if (jdbcTemplate.update(sql, obj.getName(), obj.getId()) > 0) {
            return obj;
        }
        return obj;
    }

    @Override
    public Category find(Long id) {
        String sql = "select * from categories where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToCategory(rs), id);

        } catch (DataRetrievalFailureException e) {
            log.warn("Категория с id {} не найдена", id);
            throw new NotFoundException(String.format("Категория с id %d не найдена", id));
        }
    }

    @Override
    public Collection<Category> findAll(Integer from, Integer size) {
        String sql = "select * from categories order by id limit :size offset :from";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("from", from);
        parameters.addValue("size", size);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToCategory(rs));
    }

    @Override
    public void delete(Long id) {
        String sql = "delete from categories where id = ?";
        if (jdbcTemplate.update(sql, id) < 0) {
            log.warn("Категория с id {} не найдена", id);
            throw new NotFoundException(String.format("Категория с id %d не найдена", id));
        }
    }

    public Collection<Category> findByIds(Collection<Long> ids) {
        String sql = "select id, name from categories a where id in (:ids)";
        SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToCategory(rs));
    }

    @SneakyThrows
    private Category mapRowToCategory(ResultSet rs) {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        return Category.builder()
                .id(id)
                .name(name)
                .build();
    }


}

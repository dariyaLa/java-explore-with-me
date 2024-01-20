package ru.practicum.subscribers;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.SubscriptionRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.users.UserRepoImpl;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class SubscriptionRepoImpl implements SubscriptionRepository<Subscription, SubscriptionState> {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final UserRepoImpl userRepo;

    @Override
    public Subscription add(Subscription subscription) {
        checkDuplicateSubscription(subscription);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("subscriptions")
                .usingGeneratedKeyColumns("id", "created_on");
        KeyHolder generatedKeys = simpleJdbcInsert.executeAndReturnKeyHolder(subscription.toMap());
        Map<String, Object> keys = generatedKeys.getKeys();
        long id = (long) Objects.requireNonNull(keys).get("id");
        Timestamp createdOn = (Timestamp) keys.get("created_on");
        subscription.setId(id);
        subscription.setCreated(createdOn.toInstant());
        return subscription;
    }

    @Override
    public Subscription find(long id) {
        String sql = "select * from subscriptions where id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToSubscriptions(rs), id);

        } catch (DataRetrievalFailureException e) {
            throw new NotFoundException(String.format("Подписка с id %d не найдена", id));
        }
    }

    @Override
    public void update(long id, long userId, SubscriptionState state) {
        find(id);
        String sql = "update subscriptions set state = :state where id = :id " +
                "and userId_subscription = :userId";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("id", id);
        parameters.addValue("userId", userId);
        parameters.addValue("state", state.toString());
        if (namedJdbcTemplate.update(sql, parameters) < 0) {
            throw new RuntimeException("Непредвиденная ошибка при смене статуса подписки");
        }
    }

    @Override
    public Collection<Subscription> findAllBySubscriber(long userId, Integer from, Integer size) {
        String sql = "select * from subscriptions where subscriber_id = :subscriberId " +
                "and state='ACTIVE' " +
                "order by id limit :size offset :from";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("subscriberId", userId);
        parameters.addValue("from", from);
        parameters.addValue("size", size);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToSubscriptions(rs));
    }

    @Override
    public Collection<Subscription> findAllByUser(long userId, Integer from, Integer size) {
        String sql = "select * from subscriptions where userId_subscription = :userSubscription " +
                "and state='OPENED' " +
                "order by id limit :size offset :from";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userSubscription", userId);
        parameters.addValue("from", from);
        parameters.addValue("size", size);
        return namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToSubscriptions(rs));
    }

    @Override
    public void closedSubscription(long closedUserId, long userId, Boolean isSubscriber) {
        String sql = null;
        userRepo.find(userId);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userId", userId);
        parameters.addValue("closedUserId", closedUserId);
        parameters.addValue("closedOn", Timestamp.from(Instant.now()));
        if (isSubscriber) {
            sql = "update subscriptions set state ='CLOSED', closed_on = :closedOn " +
                    "where subscriber_id = :userId " +
                    "and userId_subscription = :closedUserId";
        } else {
            sql = "update subscriptions set state = 'CLOSED', closed_on = :closedOn " +
                    "where subscriber_id = :closedUserId " +
                    "and userId_subscription = :userId";
        }
        if (namedJdbcTemplate.update(sql, parameters) < 0) {
            throw new RuntimeException("Непредвиденная ошибка при смене статуса подписки");
        }

    }

    private void checkDuplicateSubscription(Subscription subscription) {
        String sql = "select * from subscriptions where userId_subscription = :userSubscription " +
                "and subscriber_id = :subscriber";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userSubscription", subscription.getUserSubscription());
        parameters.addValue("subscriber", subscription.getSubscriber());
        List<Subscription> findSubsList = namedJdbcTemplate.query(sql, parameters, (rs, rowNum) -> mapRowToSubscriptions(rs));
        if (!findSubsList.isEmpty()) {
            throw new ConflictException(String.format("Подписка уже существует, пользователь c id=%s, подписчик с id=%s, " +
                            "статус %s",
                    findSubsList.get(0).getUserSubscription(), findSubsList.get(0).getSubscriber(), findSubsList.get(0).getSubscriptionState()));
        }
    }

    @SneakyThrows
    private Subscription mapRowToSubscriptions(ResultSet rs) {
        long id = rs.getLong("id");
        long userSubscription = rs.getLong("userId_subscription");
        long subscriber = rs.getLong("subscriber_id");
        String state = rs.getString("state");
        Instant createdOn = rs.getTimestamp("created_on").toInstant();
        Instant closedOn = rs.getTimestamp("closed_on") != null ? rs.getTimestamp("closed_on").toInstant() : null;
        return Subscription.builder()
                .id(id)
                .userSubscription(userSubscription)
                .subscriber(subscriber)
                .subscriptionState(SubscriptionState.getState(state))
                .created(createdOn)
                .closed(closedOn)
                .build();
    }
}

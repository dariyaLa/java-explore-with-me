package ru.practicum;

import ru.practicum.events.dto.EventUpdateDto;

import java.util.Collection;
import java.util.Map;

public interface ServiceEvents<T, K> {

    K add(T obj, Long userId);

    Collection<K> findAllAdminWithFilter(Map<String, Object> mapFilter);

    Collection<K> findAllWithFilter(Map<String, Object> mapFilter);

    Collection<K> findAllByUser(Map<String, Object> mapFilter);

    K find(Long obj, String ip);

    K findByUser(Long userId, Long eventId);

    K update(Long userId, Long eventId, EventUpdateDto obj);

    K updateByAdmin(Long eventId, EventUpdateDto obj);

}

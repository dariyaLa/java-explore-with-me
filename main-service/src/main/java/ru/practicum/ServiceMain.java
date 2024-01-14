package ru.practicum;

import java.util.Collection;

public interface ServiceMain<T, K> {

    K add(T obj);

    K update(T obj, Long id);

    K find(Long id);

    Collection<K> findAll(Integer from, Integer size);

    void delete(Long id);
}

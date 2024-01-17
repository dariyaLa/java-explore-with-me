package ru.practicum;

import java.util.Collection;

public interface RepositoryMain<T, K> {
    T add(T obj);

    T update(T obj, Long id);

    T find(Long id);

    Collection<T> findAll(Integer from, Integer size);

    void delete(Long id);
}

package ru.practicum;

import java.util.Collection;

public interface ServiceCompilations<T, K, H> {

    T add(K obj);

    T update(H obj, Long id);

    T find(Long id);

    Collection<T> findAll(Boolean pinned, int from, int size);

    void delete(Long id);
}

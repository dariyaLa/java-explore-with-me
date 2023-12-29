package ru.practicum.interfaces;

import ru.practicum.model.App;

import java.util.List;

public interface AppRepository {

    long add(App app);

    List<App> findByAppAndUri(String app, String uri);

    List<App> getAppsByUris(List<String> uris);

    List<App> getAppsByIds(List<Long> ids);
}

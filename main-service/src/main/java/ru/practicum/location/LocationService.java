package ru.practicum.location;

import ru.practicum.ServiceMain;

import java.util.Collection;

public class LocationService implements ServiceMain<Location, LocationDto> {
    @Override
    public LocationDto add(Location obj) {
        return null;
    }

    @Override
    public LocationDto update(Location obj, Long id) {
        return null;
    }

    @Override
    public LocationDto find(Long id) {
        return null;
    }

    @Override
    public Collection<LocationDto> findAll(Integer from, Integer size) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}

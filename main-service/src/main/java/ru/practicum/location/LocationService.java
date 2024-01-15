package ru.practicum.location;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ServiceMain;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService implements ServiceMain<Location, LocationDto> {

    private final LocationRepoImpl repository;

    @Override
    public LocationDto add(Location obj) {
        return LocationMapper.toLocationDto(repository.add(obj));
    }

    @Override
    public LocationDto update(Location obj, Long id) {
        return LocationMapper.toLocationDto(repository.update(obj, id));
    }

    @Override
    public LocationDto find(Long id) {
        return LocationMapper.toLocationDto(repository.find(id));
    }

    @Override
    public Collection<LocationDto> findAll(Integer from, Integer size) {
        return repository.findAll(from, size).stream()
                .map(LocationMapper::toLocationDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);
    }
}

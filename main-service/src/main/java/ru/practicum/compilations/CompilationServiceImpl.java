package ru.practicum.compilations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ServiceCompilations;
import ru.practicum.StatsIntegration;
import ru.practicum.ViewStatsDto;
import ru.practicum.categories.Category;
import ru.practicum.categories.CategoryRepoImpl;
import ru.practicum.categories.CompilationUpdateDto;
import ru.practicum.events.Event;
import ru.practicum.events.EventRepoImpl;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ValidationException;
import ru.practicum.requests.RequestRepositoryImpl;
import ru.practicum.users.User;
import ru.practicum.users.UserRepoImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements ServiceCompilations<CompilationDtoOut, CompilationDto, CompilationUpdateDto> {

    private final CompilationRepoImpl repository;
    private final CategoryRepoImpl categoryRepo;
    private final EventRepoImpl eventRepo;
    private final RequestRepositoryImpl requestRepo;
    private final UserRepoImpl userRepo;
    private final StatsIntegration statsIntegration;

    @Override
    public CompilationDtoOut add(CompilationDto compilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationDto);
        try {
            compilation = repository.add(compilation);
        } catch (RuntimeException e) {
            checkException(e, compilation);
        }
        if (!compilationDto.getEvents().isEmpty()) {
            repository.addEventsByCompId(compilation.getId(), compilation.getEvents());
        }
        return compilationDtoFullOut(compilation);
    }


    @Override
    public CompilationDtoOut update(CompilationUpdateDto compilationUpdateDto, Long id) {
        Compilation compilation = repository.find(id);
        updateNotNullFields(compilation, compilationUpdateDto);
        try {
            compilation = repository.update(compilation);
        } catch (RuntimeException e) {
            checkException(e, compilation);
        }
        Collection<Long> eventIds = compilationUpdateDto.getEvents();
        if (eventIds != null && !eventIds.isEmpty()) {
            repository.removeEventsByCompId(id);
            repository.addEventsByCompId(id, eventIds);
            compilation.setEvents(compilationUpdateDto.getEvents());
        }
        return compilationDtoFullOut(compilation);
    }

    @Override
    public CompilationDtoOut find(Long id) {
        Compilation compilation = repository.find(id);
        addEventIdsToCompilation(compilation);
        return compilationDtoFullOut(compilation);
    }

    @Override
    public Collection<CompilationDtoOut> findAll(Boolean pinned, int from, int size) {
        Collection<Compilation> compilations;
        if (pinned != null) {
            compilations = repository.findAllWithFilter(pinned, from, size);
        } else {
            compilations = repository.findAll(from, size);
        }

        if (compilations.isEmpty()) {
            return Collections.emptyList();
        }
        addEventIdsToCompilations(compilations);
        return compilationDtoList(compilations);
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);
    }

    private CompilationDtoOut compilationDtoFullOut(Compilation compilation) {
        Collection<Long> eventIds = compilation.getEvents();
        if (eventIds == null || eventIds.isEmpty()) {
            return CompilationMapper.toCompilationDto(compilation);
        }
        Collection<Event> events = eventRepo.findByIds(eventIds);
        Collection<Category> categories = getCategoriesByEvents(events);
        Collection<User> users = getUsersByEvents(events);
        Map<Long, Integer> confirmedRequestsByEventIds =
                requestRepo.countConfirmedRequestsByEventIds(eventIds);
        Collection<ViewStatsDto> viewStatsDtos = statsIntegration.statsRequest(events);

        return CompilationMapper.toCompilationDtoFull(compilation, events,
                categories, users, confirmedRequestsByEventIds, viewStatsDtos);
    }

    private Collection<CompilationDtoOut> compilationDtoList(Collection<Compilation> compilations) {
        List<Long> eventIds = compilations.stream()
                .map(Compilation::getEvents)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        if (eventIds.isEmpty()) {
            return compilations.stream()
                    .map(CompilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        }
        Collection<Event> events = eventRepo.findByIds(eventIds);
        Collection<Category> categories = getCategoriesByEvents(events);
        Collection<User> users = getUsersByEvents(events);
        Map<Long, Integer> confirmedRequestsByEventIds =
                requestRepo.countConfirmedRequestsByEventIds(eventIds);
        Collection<ViewStatsDto> viewStatsDtos = statsIntegration.statsRequest(events);
        return CompilationMapper.toCompilationDtoFull(compilations, events, categories, users,
                confirmedRequestsByEventIds, viewStatsDtos);
    }

    private Collection<Category> getCategoriesByEvents(Collection<Event> events) {
        Collection<Long> categoryIds = events.stream()
                .map(Event::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        return categoryRepo.findByIds(categoryIds);
    }

    private Collection<User> getUsersByEvents(Collection<Event> events) {
        Collection<Long> userIds = events.stream()
                .map(Event::getInitiator)
                .distinct()
                .collect(Collectors.toList());
        return userRepo.findByIds(userIds);
    }

    private void addEventIdsToCompilation(Compilation compilation) {
        Collection<Long> eventIds = repository.findEventIdsByCompId(compilation.getId());
        compilation.setEvents(eventIds);
    }

    private void addEventIdsToCompilations(Collection<Compilation> compilations) {
        if (compilations.isEmpty()) {
            return;
        }
        Collection<Long> compIds = compilations.stream().map(Compilation::getId).collect(Collectors.toList());
        Map<Long, Collection<Long>> eventIdsByCompIds = repository.findEventIdsByCompIds(compIds);
        compilations.forEach(compilation -> compilation.setEvents(eventIdsByCompIds.get(compilation.getId())));
    }

    private void updateNotNullFields(Compilation compilation,
                                     CompilationUpdateDto updateDto) {
        if (updateDto.getPinned() != null) {
            compilation.setPinned(updateDto.getPinned());
        }
        if (updateDto.getTitle() != null) {
            validateStringField(updateDto.getTitle(), "название подборки", 1, 50);
            compilation.setTitle(updateDto.getTitle());
        }
    }

    private void checkException(RuntimeException e, Compilation compilation) {
        String error = e.getMessage();
        String constraint = "uq_compilation_title";
        if (error.contains(constraint)) {
            error = String.format("Подборка с названием %s уже существует", compilation.getTitle());
            log.warn("Попытка дублирования названия подборки: {}", compilation.getTitle());
            throw new ConflictException(error);
        }
        throw new RuntimeException("Ошибка при передаче данных в БД");
    }

    public static void validateStringField(String field, String fieldName, int min, int max) {
        if (field.isBlank()) {
            throw new ValidationException(String.format("Поле %s не может быть пустым", fieldName));
        }
        if (field.length() < min || field.length() > max) {
            throw new ValidationException(String.format("Длина поля %s должна быть от %d до %d", fieldName, min, max));
        }
    }
}

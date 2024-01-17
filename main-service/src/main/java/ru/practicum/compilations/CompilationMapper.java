package ru.practicum.compilations;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ViewStatsDto;
import ru.practicum.categories.Category;
import ru.practicum.events.Event;
import ru.practicum.events.dto.EventMapper;
import ru.practicum.events.dto.EventShortDtoOut;
import ru.practicum.users.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompilationMapper {

    public static Compilation toCompilation(CompilationDto compilationDto) {
        Compilation compilation = Compilation.builder()
                .pinned(compilationDto.isPinned())
                .title(compilationDto.getTitle())
                .events(compilationDto.getEvents())
                .build();
        return compilation;
    }

    public static CompilationDtoOut toCompilationDto(Compilation compilation) {
        return CompilationDtoOut.builder()
                .id(compilation.getId())
                .pinned(compilation.isPinned())
                .title(compilation.getTitle())
                .events(new ArrayList<>())
                .build();
    }

    public static CompilationDtoOut toCompilationDtoFull(Compilation compilation,
                                                         Collection<Event> events,
                                                         Collection<Category> categories,
                                                         Collection<User> users,
                                                         Map<Long, Integer> confirmedRequestsByEventId,
                                                         Collection<ViewStatsDto> viewStatsDtos) {
        CompilationDtoOut compilationDto = toCompilationDto(compilation);
        Collection<EventShortDtoOut> eventShortDtos = EventMapper.toEventShortDto(
                events,
                categories,
                users,
                confirmedRequestsByEventId,
                viewStatsDtos
        );
        compilationDto.setEvents(eventShortDtos);
        return compilationDto;
    }

    public static Collection<CompilationDtoOut> toCompilationDtoFull(Collection<Compilation> compilations,
                                                                     Collection<Event> events,
                                                                     Collection<Category> categories,
                                                                     Collection<User> users,
                                                                     Map<Long, Integer> confirmedRequestsByEventId,
                                                                     Collection<ViewStatsDto> viewStatsDtos) {
        Collection<EventShortDtoOut> eventShortDtos = EventMapper.toEventShortDto(
                events,
                categories,
                users,
                confirmedRequestsByEventId,
                viewStatsDtos
        );
        Map<Long, EventShortDtoOut> eventDtoByEventIds = eventShortDtos.stream()
                .collect(Collectors.toMap(EventShortDtoOut::getId, Function.identity()));


        return compilations.stream()
                .map(compilation -> {
                    CompilationDtoOut compilationDto = toCompilationDto(compilation);
                    Collection<EventShortDtoOut> eventShortDtoForCompilation =
                            compilation.getEvents().stream()
                                    .map(eventDtoByEventIds::get).collect(Collectors.toList());
                    compilationDto.setEvents(eventShortDtoForCompilation);
                    return compilationDto;
                })
                .collect(Collectors.toList());
    }
}

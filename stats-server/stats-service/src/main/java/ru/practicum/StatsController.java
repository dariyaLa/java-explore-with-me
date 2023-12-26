package ru.practicum;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@Validated
public class StatsController {

    private final StatsServiceImpl service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void addHit(@RequestBody @Valid HitDto hitDto) {
        service.addHit(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStat(@RequestParam String start,
                                      @RequestParam String end,
                                      @RequestParam(required = false, defaultValue = "") List<String> uris,
                                      @RequestParam(required = false, defaultValue = "false") boolean unique) {
        return service.getStats(start, end, uris, unique);
    }
}

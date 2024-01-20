package ru.practicum.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ApiResponse;

import javax.validation.Valid;
import java.util.Collection;

import static ru.practicum.constants.Constant.SUCCESSFUL;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@Validated
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDtoOut add(@Valid @RequestBody UserDto userDto) {
        return service.add(userDto);
    }

    @GetMapping
    public Collection<UserDtoOut> findAll(@RequestParam(defaultValue = "") Collection<Long> ids,
                                          @RequestParam(defaultValue = "0") Integer from,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return service.findAllIds(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        service.delete(userId);
    }

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse closedProfile(@PathVariable long userId,
                                     @RequestParam boolean isPublicProfile) {
        service.changeProfile(userId, isPublicProfile);
        return ApiResponse.builder().stateAction(SUCCESSFUL).build();
    }
}

package ru.practicum.users;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDtoOut {

    private long id;
    private String email;
    private String name;
}

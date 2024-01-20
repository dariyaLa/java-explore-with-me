package ru.practicum.users;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class User {

    private long id;
    private String email;
    private String name;
    private boolean publicProfile = Boolean.TRUE;

    public Map<String, Object> toMap() {
        return Map.of(
                "name", name,
                "email", email,
                "publicProfile", publicProfile
        );
    }
}

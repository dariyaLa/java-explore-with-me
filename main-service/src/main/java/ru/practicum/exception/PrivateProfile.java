package ru.practicum.exception;

import lombok.Getter;

@Getter
public class PrivateProfile extends RuntimeException {

    private long id;

    public PrivateProfile(String message, long id) {
        super(message);
        this.id = id;
    }
}

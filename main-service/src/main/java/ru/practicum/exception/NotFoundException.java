package ru.practicum.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException set(String exception) {
        throw new NotFoundException(exception);
    }
}

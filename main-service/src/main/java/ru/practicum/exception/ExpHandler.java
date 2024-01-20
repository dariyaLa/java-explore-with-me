package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class ExpHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError exceptionHandleIncorrect(final MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Incorrectly made request.")
                .message("Ошибка валидации поля "
                        + Objects.requireNonNull(Objects.requireNonNull(e.getFieldError()).getField())
                        + ". "
                        + Objects.requireNonNull(e.getAllErrors().get(0).getDefaultMessage()))
                .timestamp(LocalDateTime.now())
                .build();

    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError exceptionHandleNotFound(final MissingServletRequestParameterException e) {
        log.error(e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Incorrectly made request.")
                .message("Отсутствует параметр " + e.getParameterName() + " в запросе")
                .timestamp(LocalDateTime.now())
                .build();

    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError exceptionHandleNotFound(final NotFoundException e) {
        log.error(e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason("The required object was not found.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError exceptionHandleConflict(final ConflictException e) {
        log.error(e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError exceptionHandleValidation(final ValidationException e) {
        log.error(e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(StateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError exceptionHandleStateException(final StateException e) {
        log.error(e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Unknown state.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(PrivateProfile.class)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiError exceptionHandlePrivateProfile(final PrivateProfile e) {
        log.error(e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.ACCEPTED)
                .reason("Successful request. id=" + e.getId())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}

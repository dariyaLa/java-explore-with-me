package ru.practicum.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.model.ErrorResponse;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
public class ExpHandler {

    ErrorResponse errors = new ErrorResponse();

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> exceptionHandleIncorrect(final MethodArgumentNotValidException e) {
        errors.setMessageError("Field " + Objects.requireNonNull(Objects.requireNonNull(e.getFieldError()).getField()) +
                " " + Objects.requireNonNull(e.getAllErrors().get(0).getDefaultMessage()));
        return new ResponseEntity<Map<String, String>>(
                Map.of("messageError", errors.getMessageError()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> exceptionHandleMissing(final MissingServletRequestParameterException e) {
        errors.setMessageError("В запросе ожидается параметр "
                + Objects.requireNonNull(Objects.requireNonNull(e.getParameterName())));
        return new ResponseEntity<Map<String, String>>(
                Map.of("messageError", errors.getMessageError()),
                HttpStatus.BAD_REQUEST
        );
    }


}

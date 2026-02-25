package org.example.library.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ExceptionResponse handleNotFoundException(NotFoundException exception) {
        log.warn("[NOT_FOUND] {}", exception.getMessage());
        return ExceptionResponse.of(exception.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(BAD_REQUEST)
    public ExceptionResponse handleBadRequestException(BadRequestException exception) {
        log.warn("[BAD_REQUEST] {}", exception.getMessage());
        return ExceptionResponse.of(exception.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(BAD_REQUEST)
    public ExceptionResponse handleValidationException(ValidationException exception) {
        log.warn("[VALIDATION] {}", exception.getMessage());
        return ExceptionResponse.of(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public ExceptionResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
                .toList();
        log.warn("[VALIDATION] {}", errors);
        return ExceptionResponse.of(errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(UNAUTHORIZED)
    public ExceptionResponse handleBadCredentialsException() {
        log.warn("[BAD_CREDENTIALS] Invalid username or password");
        return ExceptionResponse.of("Invalid username or password");
    }

    @ExceptionHandler(InvalidSortParameterException.class)
    @ResponseStatus(BAD_REQUEST)
    public ExceptionResponse handleInvalidSortParameterException(InvalidSortParameterException exception) {
        log.warn("[INVALID_SORT] {}", exception.getMessage());
        return ExceptionResponse.of(exception.getMessage());
    }

    @ExceptionHandler(InvalidPaginationParameterException.class)
    @ResponseStatus(BAD_REQUEST)
    public ExceptionResponse handleInvalidPaginationParameterException(InvalidPaginationParameterException exception) {
        log.warn("[INVALID_PAGINATION] {}", exception.getMessage());
        return ExceptionResponse.of(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleAllExceptions(Exception exception) {
        log.error("[INTERNAL_ERROR] {}", exception.getMessage(), exception);
        return ExceptionResponse.of("Something went wrong on our side... Try again later!");
    }

}

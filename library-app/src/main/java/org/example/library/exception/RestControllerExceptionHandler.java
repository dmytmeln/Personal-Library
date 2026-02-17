package org.example.library.exception;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ExceptionResponse handleNotFoundException(NotFoundException exception) {
        return ExceptionResponse.of(exception.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(BAD_REQUEST)
    public ExceptionResponse handleBadRequestException(BadRequestException exception) {
        return ExceptionResponse.of(exception.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(UNAUTHORIZED)
    public ExceptionResponse handleBadCredentialsException() {
        return ExceptionResponse.of("Invalid username or password");
    }

    @ExceptionHandler(InvalidSortParameterException.class)
    @ResponseStatus(BAD_REQUEST)
    public ExceptionResponse handleInvalidSortParameterException(InvalidSortParameterException exception) {
        return ExceptionResponse.of(exception.getMessage());
    }

    @ExceptionHandler(InvalidPaginationParameterException.class)
    @ResponseStatus(BAD_REQUEST)
    public ExceptionResponse handleInvalidPaginationParameterException(InvalidPaginationParameterException exception) {
        return ExceptionResponse.of(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleAllExceptions() {
        return ExceptionResponse.of("Something went wrong on our side... Try again later!");
    }

}

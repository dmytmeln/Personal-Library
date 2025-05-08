package org.example.library.exception;


public record ExceptionResponse(String message) {
    public static ExceptionResponse of(String message) {
        return new ExceptionResponse(message);
    }
}

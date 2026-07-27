package org.example.library.common.exception;

public class BadRequestException extends RuntimeException {

    // todo system message and user message

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}

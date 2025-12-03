package com.luizalabs.orders.domain.exception;

public class InvalidFileFormatException extends DomainException {

    public InvalidFileFormatException(String message) {
        super(message);
    }

    public InvalidFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}

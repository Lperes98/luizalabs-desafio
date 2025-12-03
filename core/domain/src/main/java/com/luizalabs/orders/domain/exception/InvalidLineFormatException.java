package com.luizalabs.orders.domain.exception;

public class InvalidLineFormatException extends DomainException {

    public InvalidLineFormatException(String message) {
        super(message);
    }

    public InvalidLineFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}

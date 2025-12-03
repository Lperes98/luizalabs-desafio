package com.luizalabs.orders.domain.exception;

public class FileProcessingException extends DomainException {

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

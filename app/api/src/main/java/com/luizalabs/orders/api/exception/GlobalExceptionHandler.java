package com.luizalabs.orders.api.exception;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.*;

import com.luizalabs.orders.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({EmptyFileException.class, InvalidFileFormatException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(DomainException ex) {
        log.error("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(buildError(BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorResponse> handleProcessingError(FileProcessingException ex) {
        log.error("Erro ao processar: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(buildError(INTERNAL_SERVER_ERROR, "Erro ao processar arquivo"));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        log.error("Não encontrado: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(buildError(NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleFileTooLarge(MaxUploadSizeExceededException ex) {
        log.error("Arquivo muito grande");
        return ResponseEntity.status(PAYLOAD_TOO_LARGE)
                .body(buildError(PAYLOAD_TOO_LARGE, "Arquivo excede tamanho máximo"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(buildError(INTERNAL_SERVER_ERROR, "Erro inesperado"));
    }

    private ErrorResponse buildError(HttpStatus status, String message) {
        return ErrorResponse.builder()
                .timestamp(now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
    }
}

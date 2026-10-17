package com.eripy.banking_system.exceptions;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice 
public class GlobalExceptionHandler {
    @ExceptionHandler({FormatDataException.class, InvalidQuantity.class})
    public ResponseEntity<Map<String, String>> handleInvalidaFormat(
        Exception e
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MailDeliveryException.class)
    public ResponseEntity<Map<String, String>> handleServiceFailed(
        MailDeliveryException e
    ) {
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
        ResourceNotFoundException e
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", e.getMessage()));
    }
}

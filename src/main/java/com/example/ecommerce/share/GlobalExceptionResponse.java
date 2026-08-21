package com.example.ecommerce.share;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@ControllerAdvice
public class GlobalExceptionResponse {

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<GlobalResponse<String>> handleNoResourceFoundException(NoResourceFoundException ex) {
        var errors = List.of(
            new GlobalResponse.ErrorItems(ex.getMessage())
        );

        return ResponseEntity.status(404).body(new GlobalResponse<String>(errors));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<GlobalResponse<?>> handleCustomResException(CustomException ex) {
        var errors = List.of(
            new GlobalResponse.ErrorItems(ex.getMessage())
        );

        return ResponseEntity.status(ex.getStatusCode()).body(new GlobalResponse<>(errors));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new GlobalResponse.ErrorItems(
                        fieldError.getField() + ": " + fieldError.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity.status(400).body(new GlobalResponse<>(errors));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<GlobalResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        var errors = List.of(
                new GlobalResponse.ErrorItems("Invalid username or password")
        );

        return ResponseEntity.status(401).body(new GlobalResponse<>(errors));
    }
}

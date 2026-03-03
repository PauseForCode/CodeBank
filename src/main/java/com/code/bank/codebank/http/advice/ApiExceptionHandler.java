package com.code.bank.codebank.http.advice;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.code.bank.codebank.application.snippet.service.SnippetNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SnippetNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(SnippetNotFoundException exception) {
        ApiError error = new ApiError("SNIPPET_NOT_FOUND", exception.getMessage(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        ApiError error = new ApiError("VALIDATION_ERROR", "Invalid request payload", Instant.now());
        return ResponseEntity.badRequest().body(error);
    }
}

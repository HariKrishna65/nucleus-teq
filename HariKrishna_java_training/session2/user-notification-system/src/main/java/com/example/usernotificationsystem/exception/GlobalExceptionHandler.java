package com.example.usernotificationsystem.exception;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleException(RuntimeException ex) {
        return Map.of("error", ex.getMessage());
    }
}
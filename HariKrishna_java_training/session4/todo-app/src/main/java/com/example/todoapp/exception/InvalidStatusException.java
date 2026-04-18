package com.example.todoapp.exception;

public class InvalidStatusException extends RuntimeException {
    public InvalidStatusException(String msg) {
        super(msg);
    }
}
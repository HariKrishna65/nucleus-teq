package com.interview.tracker.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBadRequest_returnsStructuredBadRequestBody() {
        ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(new IllegalArgumentException("Bad input"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Bad input", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleServerError_withoutMessage_usesFallback() {
        ResponseEntity<Map<String, Object>> response = handler.handleServerError(new RuntimeException());

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Something went wrong", response.getBody().get("message"));
    }
}

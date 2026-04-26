package com.interview.tracker.dto;

public class AuthResponse {

    private String message;
    private String role;
    private Long userId;

    public AuthResponse() {}

    public AuthResponse(String message, String role, Long userId) {
        this.message = message;
        this.role = role;
        this.userId = userId;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}

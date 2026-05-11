package com.interview.tracker.dto;

public class AuthResponse {

    private String message;
    private String role;
    private Long userId;
    private String name;
    private Long panelId;
    private String token;

    public AuthResponse() {}

    public AuthResponse(String message, String role, Long userId) {
        this.message = message;
        this.role = role;
        this.userId = userId;
    }

    public AuthResponse(String message, String role, Long userId, String name, Long panelId) {
        this.message = message;
        this.role = role;
        this.userId = userId;
        this.name = name;
        this.panelId = panelId;
    }

    public AuthResponse(String message, String role, Long userId, String name, Long panelId, String token) {
        this.message = message;
        this.role = role;
        this.userId = userId;
        this.name = name;
        this.panelId = panelId;
        this.token = token;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getPanelId() { return panelId; }
    public void setPanelId(Long panelId) { this.panelId = panelId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}

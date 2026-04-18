package com.example.todoapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TodoDTO {

    @NotNull
    @Size(min = 3)
    private String title;

    private String description;

    private String status;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

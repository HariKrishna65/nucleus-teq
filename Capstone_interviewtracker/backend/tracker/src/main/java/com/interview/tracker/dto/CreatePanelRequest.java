package com.interview.tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreatePanelRequest {
    @NotBlank(message = "Panel name is required")
    @Size(min = 2, max = 80, message = "Name must be between 2 and 80 characters")
    private String name;

    @NotBlank(message = "Panel email is required")
    @Email(message = "Enter a valid panel email")
    private String email;

    @Pattern(regexp = "^[0-9+\\-()\\s]{7,20}$", message = "Enter a valid panel phone number")
    private String phone;

    @NotBlank(message = "Organization is required")
    private String organization;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotBlank(message = "Expertise is required")
    private String expertise;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = expertise; }
}


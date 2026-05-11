package com.interview.tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public class ReferralCandidateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-()\\s]{7,20}$", message = "Enter a valid phone number")
    private String phone;

    @NotNull(message = "Job description is required")
    private Long jdId;

    @PositiveOrZero(message = "Experience cannot be negative")
    private Double experience;

    private String source;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Long getJdId() { return jdId; }
    public void setJdId(Long jdId) { this.jdId = jdId; }

    public Double getExperience() { return experience; }
    public void setExperience(Double experience) { this.experience = experience; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}

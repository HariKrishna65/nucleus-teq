package com.interview.tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "job_description")
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Skills are required")
    private String skills;

    @NotNull(message = "Minimum experience is required")
    @Min(value = 0, message = "Minimum experience cannot be negative")
    private Integer experienceMin;

    @NotNull(message = "Maximum experience is required")
    @Min(value = 0, message = "Maximum experience cannot be negative")
    private Integer experienceMax;

    @NotNull(message = "Minimum salary is required")
    @Min(value = 0, message = "Minimum salary cannot be negative")
    private Integer salaryMin;

    @NotNull(message = "Maximum salary is required")
    @Min(value = 0, message = "Maximum salary cannot be negative")
    private Integer salaryMax;

    private Integer experience;
    private String salary;
    private String location;
    private String jobType;

    public JobDescription() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public Integer getExperienceMin() { return experienceMin; }
    public void setExperienceMin(Integer experienceMin) { this.experienceMin = experienceMin; }

    public Integer getExperienceMax() { return experienceMax; }
    public void setExperienceMax(Integer experienceMax) { this.experienceMax = experienceMax; }

    public Integer getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }

    public Integer getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }

    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
}

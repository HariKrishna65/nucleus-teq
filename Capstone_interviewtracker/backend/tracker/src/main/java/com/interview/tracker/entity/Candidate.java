package com.interview.tracker.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.interview.tracker.constants.Stage;
import com.interview.tracker.constants.StageStatus;

@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9+\\-()\\s]{7,20}$", message = "Enter a valid phone number")
    private String phone;

    @PositiveOrZero(message = "Experience cannot be negative")
    private Double experience;
    private String status;

    private LocalDateTime applicationDate;

    // Stage tracking (SRS)
    @Enumerated(EnumType.STRING)
    private Stage stage;

    @Enumerated(EnumType.STRING)
    private StageStatus stageStatus;

    private String hrComments;

    // SRS fields (nullable / migration-safe)
    private String fullName;
    private String mobileCode;
    private String mobileNumber;
    private LocalDate dateOfBirth;
    private String currentOrganization;
    private Double totalExperience;
    private Double relevantExperience;
    private Double currentCtc;
    private Double expectedCtc;
    private Integer noticePeriodDays;
    private String preferredLocation;
    private String source;

    @Column(name = "resume_url")
    private String resumeUrl;

    @ManyToOne
    @JoinColumn(name = "jd_id")
    private JobDescription jd;

    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    public Candidate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getExperience() { return experience; }
    public void setExperience(Double experience) { this.experience = experience; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDateTime applicationDate) { this.applicationDate = applicationDate; }

    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }

    public StageStatus getStageStatus() { return stageStatus; }
    public void setStageStatus(StageStatus stageStatus) { this.stageStatus = stageStatus; }

    public String getHrComments() { return hrComments; }
    public void setHrComments(String hrComments) { this.hrComments = hrComments; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getMobileCode() { return mobileCode; }
    public void setMobileCode(String mobileCode) { this.mobileCode = mobileCode; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getCurrentOrganization() { return currentOrganization; }
    public void setCurrentOrganization(String currentOrganization) { this.currentOrganization = currentOrganization; }

    public Double getTotalExperience() { return totalExperience; }
    public void setTotalExperience(Double totalExperience) { this.totalExperience = totalExperience; }

    public Double getRelevantExperience() { return relevantExperience; }
    public void setRelevantExperience(Double relevantExperience) { this.relevantExperience = relevantExperience; }

    public Double getCurrentCtc() { return currentCtc; }
    public void setCurrentCtc(Double currentCtc) { this.currentCtc = currentCtc; }

    public Double getExpectedCtc() { return expectedCtc; }
    public void setExpectedCtc(Double expectedCtc) { this.expectedCtc = expectedCtc; }

    public Integer getNoticePeriodDays() { return noticePeriodDays; }
    public void setNoticePeriodDays(Integer noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }

    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String preferredLocation) { this.preferredLocation = preferredLocation; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }

    public JobDescription getJd() { return jd; }
    public void setJd(JobDescription jd) { this.jd = jd; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
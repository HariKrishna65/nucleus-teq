package com.interview.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(nullable = false)
    private String interviewerName;

    private String interviewerEmail;

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    private Integer durationMinutes = 60;

    @Enumerated(EnumType.STRING)
    private Round round = Round.PHONE_SCREEN;

    @Enumerated(EnumType.STRING)
    private Status status = Status.SCHEDULED;

    private String meetingLink;

    private String notes;

    public enum Round {
        PHONE_SCREEN, TECHNICAL, MANAGER, HR, FINAL
    }

    public enum Status {
        SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }
    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }
    public String getInterviewerEmail() { return interviewerEmail; }
    public void setInterviewerEmail(String interviewerEmail) { this.interviewerEmail = interviewerEmail; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Round getRound() { return round; }
    public void setRound(Round round) { this.round = round; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
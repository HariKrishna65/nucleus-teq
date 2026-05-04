package com.interview.tracker.entity;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Round is required")
    @Pattern(regexp = "^(L1|L2|HR|FINAL)$", message = "Round must be L1, L2, HR or FINAL")
    private String round;

    @NotNull(message = "Interview time is required")
    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime interviewTime;

    @NotBlank(message = "Focus area is required")
    private String focusArea;

    @Column(name = "interviewer_name", nullable = false)
    private String interviewerName;

    private String status;

    private Integer duration;

    private String interviewType;

    private String meetingLink;

    @Column(length = 1000)
    private String notes;

    
    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    
    @ManyToOne
    @JoinColumn(name = "panel_id")
    private Panel panel;

    @ManyToMany
    @JoinTable(
            name = "interview_panels",
            joinColumns = @JoinColumn(name = "interview_id"),
            inverseJoinColumns = @JoinColumn(name = "panel_id")
    )
    private List<Panel> panels = new ArrayList<>();

    public Interview() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRound() { return round; }
    public void setRound(String round) { this.round = round; }

    public LocalDateTime getInterviewTime() { return interviewTime; }
    public void setInterviewTime(LocalDateTime interviewTime) { this.interviewTime = interviewTime; }

    public String getFocusArea() { return focusArea; }
    public void setFocusArea(String focusArea) { this.focusArea = focusArea; }

    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getInterviewType() { return interviewType; }
    public void setInterviewType(String interviewType) { this.interviewType = interviewType; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public Panel getPanel() { return panel; }
    public void setPanel(Panel panel) { this.panel = panel; }

    public List<Panel> getPanels() { return panels; }
    public void setPanels(List<Panel> panels) { this.panels = panels; }
}

package com.interview.tracker.entity;

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
    private String round; 

    @NotNull(message = "Interview time is required")
    private LocalDateTime interviewTime;

    @NotBlank(message = "Focus area is required")
    private String focusArea;

    private String status;

    
    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    
    @ManyToOne
    @JoinColumn(name = "panel_id")
    private Panel panel;

    // New: support 1-2 panel members (migration-safe: join table)
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public Panel getPanel() { return panel; }
    public void setPanel(Panel panel) { this.panel = panel; }

    public List<Panel> getPanels() { return panels; }
    public void setPanels(List<Panel> panels) { this.panels = panels; }
}
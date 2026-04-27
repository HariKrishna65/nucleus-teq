package com.interview.tracker.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String round; 

    private LocalDateTime interviewTime;

    private String focusArea;

    
    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    
    @ManyToOne
    @JoinColumn(name = "panel_id")
    private Panel panel;

    public Interview() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRound() { return round; }
    public void setRound(String round) { this.round = round; }

    public LocalDateTime getInterviewTime() { return interviewTime; }
    public void setInterviewTime(LocalDateTime interviewTime) { this.interviewTime = interviewTime; }

    public String getFocusArea() { return focusArea; }
    public void setFocusArea(String focusArea) { this.focusArea = focusArea; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public Panel getPanel() { return panel; }
    public void setPanel(Panel panel) { this.panel = panel; }
}
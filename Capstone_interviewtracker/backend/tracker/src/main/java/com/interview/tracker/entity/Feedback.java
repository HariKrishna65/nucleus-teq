package com.interview.tracker.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.*;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Comments are mandatory")
    private String comments;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
    private String result;

    private String strengths;
    private String weaknesses;
    private String areasCovered;

    @ManyToOne
    @JoinColumn(name = "interview_id")
    private Interview interview;

    @ManyToOne
    @JoinColumn(name = "panel_id")
    private Panel panel;

    public Feedback() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getStatus() { return result; }
    public void setStatus(String status) { this.result = status; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String weaknesses) { this.weaknesses = weaknesses; }

    public String getAreasCovered() { return areasCovered; }
    public void setAreasCovered(String areasCovered) { this.areasCovered = areasCovered; }

    public Interview getInterview() { return interview; }
    public void setInterview(Interview interview) { this.interview = interview; }

    public Panel getPanel() { return panel; }
    public void setPanel(Panel panel) { this.panel = panel; }
}
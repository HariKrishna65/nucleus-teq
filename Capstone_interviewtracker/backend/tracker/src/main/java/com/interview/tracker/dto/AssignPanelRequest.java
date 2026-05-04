package com.interview.tracker.dto;

import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

public class AssignPanelRequest {

    @NotEmpty(message = "At least one panel email is required")
    private List<String> panelEmails;

    private LocalDateTime interviewTime;

    private Integer duration;

    private String interviewType;

    private String meetingLink;

    private String focusArea;

    private String notes;

    public List<String> getPanelEmails() { return panelEmails; }
    public void setPanelEmails(List<String> panelEmails) { this.panelEmails = panelEmails; }

    public LocalDateTime getInterviewTime() { return interviewTime; }
    public void setInterviewTime(LocalDateTime interviewTime) { this.interviewTime = interviewTime; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getInterviewType() { return interviewType; }
    public void setInterviewType(String interviewType) { this.interviewType = interviewType; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getFocusArea() { return focusArea; }
    public void setFocusArea(String focusArea) { this.focusArea = focusArea; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

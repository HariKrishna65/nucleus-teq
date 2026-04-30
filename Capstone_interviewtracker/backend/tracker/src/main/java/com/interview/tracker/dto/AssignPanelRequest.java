package com.interview.tracker.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AssignPanelRequest {

    @NotEmpty(message = "At least one panel email is required")
    private List<String> panelEmails;

    public List<String> getPanelEmails() { return panelEmails; }
    public void setPanelEmails(List<String> panelEmails) { this.panelEmails = panelEmails; }
}


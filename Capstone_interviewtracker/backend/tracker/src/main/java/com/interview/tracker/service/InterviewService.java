package com.interview.tracker.service;

import com.interview.tracker.entity.Interview;
import com.interview.tracker.repository.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterviewService {

    @Autowired
    private InterviewRepository interviewRepository;

    public Interview scheduleInterview(Interview interview) {
        if (interview.getPanels() != null && interview.getPanels().size() > 2) {
            throw new IllegalArgumentException("Maximum 2 panel members allowed per interview");
        }
        if (interview.getPanels() != null && interview.getPanels().size() == 1 && interview.getPanel() == null) {
            interview.setPanel(interview.getPanels().get(0));
        }
        if (interview.getStatus() == null || interview.getStatus().isBlank()) {
            interview.setStatus("PENDING");
        }
        return interviewRepository.save(interview);
    }

    public List<Interview> getByPanel(Long panelId) {
        return interviewRepository.findAssignedToPanel(panelId);
    }

    public Interview getById(Long id) {
        return interviewRepository.findById(id).orElse(null);
    }
}
package com.interview.tracker.service;

import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.repository.InterviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;

    public InterviewService(InterviewRepository interviewRepository) {
        this.interviewRepository = interviewRepository;
    }

    public Interview scheduleInterview(Interview interview) {
        if (interview.getPanels() != null && interview.getPanels().size() > 2) {
            throw new IllegalArgumentException("Maximum 2 panel members allowed per interview");
        }
        if (interview.getPanels() != null && interview.getPanels().size() == 1 && interview.getPanel() == null) {
            interview.setPanel(interview.getPanels().get(0));
        }
        if (interview.getInterviewerName() == null || interview.getInterviewerName().isBlank()) {
            interview.setInterviewerName(buildInterviewerName(interview));
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

    private String buildInterviewerName(Interview interview) {
        List<Panel> panels = interview.getPanels();
        if (panels != null && !panels.isEmpty()) {
            String joined = panels.stream()
                    .map(p -> p.getName() != null && !p.getName().isBlank() ? p.getName() : p.getEmail())
                    .filter(s -> s != null && !s.isBlank())
                    .distinct()
                    .collect(Collectors.joining(", "));
            if (!joined.isBlank()) return joined;
        }
        Panel p = interview.getPanel();
        if (p != null) {
            if (p.getName() != null && !p.getName().isBlank()) return p.getName();
            if (p.getEmail() != null && !p.getEmail().isBlank()) return p.getEmail();
        }
        return "Panel";
    }
}
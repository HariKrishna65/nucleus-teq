package com.interview.tracker.service;

import com.interview.tracker.entity.Feedback;
import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.repository.FeedbackRepository;
import com.interview.tracker.repository.InterviewRepository;
import com.interview.tracker.repository.PanelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for feedback operations.
 */
@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private PanelRepository panelRepository;

    /**
     * Save feedback.
     */
    public Feedback save(Feedback feedback) {
        if (feedback.getInterview() == null || feedback.getInterview().getId() == null) {
            throw new IllegalArgumentException("Interview is required");
        }
        if (feedback.getComments() == null || feedback.getComments().isBlank()) {
            throw new IllegalArgumentException("Comments are mandatory");
        }
        if (feedback.getRating() == null || feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (feedback.getStatus() == null || feedback.getStatus().isBlank()) {
            throw new IllegalArgumentException("Status is required (Selected/Rejected)");
        }

        Long interviewId = feedback.getInterview().getId();
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        String role = SecurityUtil.currentRole();

        // PANEL enforcement: panel must be assigned to the interview
        String email = SecurityUtil.currentEmail();
        if ("PANEL".equals(role) && email != null) {
            Panel panel = panelRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Panel profile not found"));

            boolean assigned =
                    (interview.getPanel() != null && interview.getPanel().getId().equals(panel.getId())) ||
                    (interview.getPanels() != null && interview.getPanels().stream().anyMatch(p -> p.getId().equals(panel.getId())));

            if (!assigned) {
                throw new IllegalArgumentException("You are not assigned to this interview");
            }

            feedback.setPanel(panel);
        } else if (!"HR".equals(role)) {
            throw new IllegalArgumentException("Access denied");
        }

        // Mark interview as completed when feedback is submitted
        interview.setStatus("COMPLETED");
        interviewRepository.save(interview);

        return feedbackRepository.save(feedback);
    }
}
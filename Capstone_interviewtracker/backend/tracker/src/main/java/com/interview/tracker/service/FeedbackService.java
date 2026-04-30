package com.interview.tracker.service;

import com.interview.tracker.entity.Feedback;
import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.repository.FeedbackRepository;
import com.interview.tracker.repository.InterviewRepository;
import com.interview.tracker.repository.PanelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.interview.tracker.constants.AppConstants.ROLE_HR;
import static com.interview.tracker.constants.AppConstants.ROLE_PANEL;

@Service
public class FeedbackService {
    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository feedbackRepository;
    private final InterviewRepository interviewRepository;
    private final PanelRepository panelRepository;

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            InterviewRepository interviewRepository,
            PanelRepository panelRepository
    ) {
        this.feedbackRepository = feedbackRepository;
        this.interviewRepository = interviewRepository;
        this.panelRepository = panelRepository;
    }

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

        String email = SecurityUtil.currentEmail();
        if (ROLE_PANEL.equals(role) && email != null) {
            Panel panel = panelRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Panel profile not found"));

            boolean assigned =
                    (interview.getPanel() != null && interview.getPanel().getId().equals(panel.getId())) ||
                    (interview.getPanels() != null && interview.getPanels().stream().anyMatch(p -> p.getId().equals(panel.getId())));

            if (!assigned) {
                throw new IllegalArgumentException("You are not assigned to this interview");
            }

            feedback.setPanel(panel);
        } else if (!ROLE_HR.equals(role)) {
            throw new IllegalArgumentException("Access denied");
        }

        interview.setStatus("COMPLETED");
        interviewRepository.save(interview);
        Feedback saved = feedbackRepository.save(feedback);
        log.info("Feedback submitted: feedbackId={}, interviewId={}, role={}", saved.getId(), interviewId, role);
        return saved;
    }

    public Feedback getLatestByInterview(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        String role = SecurityUtil.currentRole();
        String email = SecurityUtil.currentEmail();
        if (ROLE_PANEL.equals(role)) {
            Panel panel = panelRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Panel profile not found"));
            boolean assigned =
                    (interview.getPanel() != null && interview.getPanel().getId().equals(panel.getId())) ||
                            (interview.getPanels() != null && interview.getPanels().stream().anyMatch(p -> p.getId().equals(panel.getId())));
            if (!assigned) {
                throw new IllegalArgumentException("You are not assigned to this interview");
            }
        } else if (!ROLE_HR.equals(role)) {
            throw new IllegalArgumentException("Access denied");
        }

        Optional<Feedback> latest = feedbackRepository.findTopByInterview_IdOrderByIdDesc(interviewId);
        return latest.orElse(null);
    }
}
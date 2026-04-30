package com.interview.tracker.controller;

import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.repository.PanelRepository;
import com.interview.tracker.service.InterviewService;
import com.interview.tracker.service.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.interview.tracker.constants.AppConstants.INTERVIEWS;
import static com.interview.tracker.constants.AppConstants.ROLE_CANDIDATE;
import static com.interview.tracker.constants.AppConstants.ROLE_HR;
import static com.interview.tracker.constants.AppConstants.ROLE_PANEL;

@RestController
@RequestMapping(INTERVIEWS)
public class InterviewController {

    private static final String PANEL_PATH = "/panel";

    private final InterviewService interviewService;
    private final PanelRepository panelRepository;

    public InterviewController(InterviewService interviewService, PanelRepository panelRepository) {
        this.interviewService = interviewService;
        this.panelRepository = panelRepository;
    }

    @PostMapping(PANEL_PATH)
    public Panel createPanel(@RequestBody Panel panel) {
        return panelRepository.save(panel);
    }

    @GetMapping(PANEL_PATH)
    public List<Panel> getPanels() {
        return panelRepository.findAll();
    }

    @PostMapping
    public Interview scheduleInterview(@Valid @RequestBody Interview interview) {
        return interviewService.scheduleInterview(interview);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Interview interview = interviewService.getById(id);
        if (interview == null) {
            return ResponseEntity.status(404).body("Interview not found");
        }

        String role = SecurityUtil.currentRole();
        Long currentUserId = SecurityUtil.currentUserId();
        String currentEmail = SecurityUtil.currentEmail();

        if (ROLE_CANDIDATE.equals(role)) {
            if (interview.getCandidate() == null || interview.getCandidate().getUser() == null ||
                    !interview.getCandidate().getUser().getId().equals(currentUserId)) {
                return ResponseEntity.status(403).body("Access denied");
            }
        } else if (ROLE_PANEL.equals(role)) {
            Panel panel = panelRepository.findByEmail(currentEmail).orElse(null);
            if (panel == null) return ResponseEntity.status(403).body("Panel profile not found");
            boolean assigned = (interview.getPanel() != null && interview.getPanel().getId().equals(panel.getId())) ||
                    (interview.getPanels() != null && interview.getPanels().stream().anyMatch(p -> p.getId().equals(panel.getId())));
            if (!assigned) return ResponseEntity.status(403).body("Access denied");
        }

        return ResponseEntity.ok(interview);
    }

    @GetMapping
    public ResponseEntity<?> getByPanel(@RequestParam Long panelId) {
        String role = SecurityUtil.currentRole();
        String currentEmail = SecurityUtil.currentEmail();

        if (ROLE_PANEL.equals(role)) {
            Panel panel = panelRepository.findByEmail(currentEmail).orElse(null);
            if (panel == null) return ResponseEntity.status(403).body("Panel profile not found");
            // For panel users, always scope to their own panel id (ignore request param)
            return ResponseEntity.ok(interviewService.getByPanel(panel.getId()));
        } else if (!ROLE_HR.equals(role)) {
            return ResponseEntity.status(403).body("Access denied");
        }

        return ResponseEntity.ok(interviewService.getByPanel(panelId));
    }
}
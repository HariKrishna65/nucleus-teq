package com.interview.tracker.controller;

import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.repository.PanelRepository;
import com.interview.tracker.service.InterviewService;
import com.interview.tracker.service.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.interview.tracker.constants.AppConstants.INTERVIEWS;

@RestController
@RequestMapping(INTERVIEWS)
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private PanelRepository panelRepository;

    @PostMapping("/panel")
    public Panel createPanel(@RequestBody Panel panel) {
        return panelRepository.save(panel);
    }

    @GetMapping("/panel")
    public List<Panel> getPanels() {
        return panelRepository.findAll();
    }

    @PostMapping
    public Interview scheduleInterview(@Valid @RequestBody Interview interview) {
        String role = SecurityUtil.currentRole();
        if (!"HR".equals(role)) {
            throw new IllegalArgumentException("Only HR can schedule interviews");
        }
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

        if ("CANDIDATE".equals(role)) {
            if (interview.getCandidate() == null || interview.getCandidate().getUser() == null ||
                    !interview.getCandidate().getUser().getId().equals(currentUserId)) {
                return ResponseEntity.status(403).body("Access denied");
            }
        } else if ("PANEL".equals(role)) {
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

        if ("PANEL".equals(role)) {
            Panel panel = panelRepository.findByEmail(currentEmail).orElse(null);
            if (panel == null || !panel.getId().equals(panelId)) {
                return ResponseEntity.status(403).body("You can only view your assigned interviews");
            }
        } else if (!"HR".equals(role)) {
            return ResponseEntity.status(403).body("Access denied");
        }

        return ResponseEntity.ok(interviewService.getByPanel(panelId));
    }
}
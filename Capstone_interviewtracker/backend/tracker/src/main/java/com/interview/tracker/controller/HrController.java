package com.interview.tracker.controller;

import com.interview.tracker.constants.Stage;
import com.interview.tracker.constants.StageStatus;
import com.interview.tracker.dto.CreatePanelRequest;
import com.interview.tracker.entity.Candidate;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.CandidateRepository;
import com.interview.tracker.repository.PanelRepository;
import com.interview.tracker.repository.UserRepository;
import com.interview.tracker.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/hr")
public class HrController {

    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final PanelRepository panelRepository;
    private final EmailService emailService;

    public HrController(
            CandidateRepository candidateRepository,
            UserRepository userRepository,
            PanelRepository panelRepository,
            EmailService emailService
    ) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.panelRepository = panelRepository;
        this.emailService = emailService;
    }

    @PostMapping("/panels")
    public ResponseEntity<?> createPanel(@Valid @RequestBody CreatePanelRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Name is required");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("User with this email already exists");
        }

        // Create USER for authentication
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole("PANEL");
        user.setEmailVerified(true); // HR is onboarding; send set-password link next
        user.setActive(true);
        user.setInvitedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        // Create Panel profile
        Panel panel = new Panel();
        panel.setName(request.getName());
        panel.setEmail(request.getEmail());
        panel.setExpertise(request.getExpertise() == null || request.getExpertise().isBlank() ? "General" : request.getExpertise());
        panel.setMobile(request.getPhone());
        panel.setOrganization(request.getOrganization());
        panel.setDesignation(request.getDesignation());
        Panel savedPanel = panelRepository.save(panel);

        // Send password setup email (secure link)
        emailService.sendPasswordSetEmail(savedUser);

        return ResponseEntity.ok(savedPanel);
    }

    @PostMapping("/candidates/{id}/advance")
    public ResponseEntity<?> advance(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        Candidate c = candidateRepository.findById(id).orElse(null);
        if (c == null) return ResponseEntity.status(404).body("Candidate not found");

        if (c.getStage() == null) {
            c.setStage(Stage.PROFILING);
            c.setStageStatus(StageStatus.COMPLETED);
        }

        if (c.getStage() == Stage.REJECTED || c.getStage() == Stage.SELECTED) {
            return ResponseEntity.badRequest().body("Candidate is already in a final stage");
        }

        Stage next = nextStage(c.getStage());
        if (next == null) return ResponseEntity.badRequest().body("No next stage available");

        c.setStage(next);
        c.setStageStatus(StageStatus.PENDING);

        if (body != null && body.containsKey("comments")) {
            c.setHrComments(body.get("comments"));
        }

        return ResponseEntity.ok(candidateRepository.save(c));
    }

    @PostMapping("/candidates/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Candidate c = candidateRepository.findById(id).orElse(null);
        if (c == null) return ResponseEntity.status(404).body("Candidate not found");

        String comments = body == null ? null : body.get("comments");
        if (comments == null || comments.isBlank()) {
            return ResponseEntity.badRequest().body("HR comments are mandatory for rejection");
        }

        c.setStage(Stage.REJECTED);
        c.setStageStatus(StageStatus.COMPLETED);
        c.setHrComments(comments);
        return ResponseEntity.ok(candidateRepository.save(c));
    }

    @PostMapping("/candidates/{id}/select")
    public ResponseEntity<?> select(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Candidate c = candidateRepository.findById(id).orElse(null);
        if (c == null) return ResponseEntity.status(404).body("Candidate not found");

        String comments = body == null ? null : body.get("comments");
        if (comments == null || comments.isBlank()) {
            return ResponseEntity.badRequest().body("HR comments are mandatory for final selection");
        }

        c.setStage(Stage.SELECTED);
        c.setStageStatus(StageStatus.COMPLETED);
        c.setHrComments(comments);
        return ResponseEntity.ok(candidateRepository.save(c));
    }

    private Stage nextStage(Stage stage) {
        return switch (stage) {
            case PROFILING -> Stage.SCREENING;
            case SCREENING -> Stage.L1_TECH;
            case L1_TECH -> Stage.L2_TECH;
            case L2_TECH -> Stage.HR_ROUND;
            case HR_ROUND -> Stage.SELECTED;
            default -> null;
        };
    }
}


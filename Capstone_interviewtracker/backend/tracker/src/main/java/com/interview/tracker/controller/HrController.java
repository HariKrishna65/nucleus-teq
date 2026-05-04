package com.interview.tracker.controller;

import com.interview.tracker.constants.Stage;
import com.interview.tracker.constants.StageStatus;
import com.interview.tracker.dto.AssignPanelRequest;
import com.interview.tracker.dto.CreatePanelRequest;
import com.interview.tracker.entity.Candidate;
import com.interview.tracker.entity.Feedback;
import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.CandidateRepository;
import com.interview.tracker.repository.FeedbackRepository;
import com.interview.tracker.repository.InterviewRepository;
import com.interview.tracker.repository.PanelRepository;
import com.interview.tracker.repository.UserRepository;
import com.interview.tracker.service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(com.interview.tracker.constants.AppConstants.HR)
public class HrController {
    private static final Logger log = LoggerFactory.getLogger(HrController.class);

    private final CandidateRepository candidateRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final PanelRepository panelRepository;
    private final EmailService emailService;
    private final InterviewRepository interviewRepository;

    @Value("${app.invite.skip-verification:true}")
    private boolean skipInviteVerification;

    public HrController(
            CandidateRepository candidateRepository,
            FeedbackRepository feedbackRepository,
            UserRepository userRepository,
            PanelRepository panelRepository,
            EmailService emailService,
            InterviewRepository interviewRepository
    ) {
        this.candidateRepository = candidateRepository;
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.panelRepository = panelRepository;
        this.emailService = emailService;
        this.interviewRepository = interviewRepository;
    }

    @GetMapping("/candidates")
    public ResponseEntity<?> getCandidatesWithProgress() {
        List<Candidate> candidates = candidateRepository.findAllByOrderByApplicationDateDesc();
        List<Map<String, Object>> payload = candidates.stream().map(candidate -> {
            List<Feedback> feedbackList = feedbackRepository.findByInterview_Candidate_IdOrderByIdDesc(candidate.getId());
            Interview currentRoundInterview = findCurrentRoundInterview(candidate);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("candidate", candidate);
            item.put("feedback", feedbackList);
            item.put("feedbackCount", feedbackList.size());
            item.put("latestFeedback", feedbackList.isEmpty() ? null : feedbackList.get(0));
            addRoundProgress(item, candidate, currentRoundInterview);
            return item;
        }).toList();
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/candidates/{id}")
    public ResponseEntity<?> getCandidateDetails(@PathVariable Long id) {
        Candidate candidate = candidateRepository.findById(id).orElse(null);
        if (candidate == null) {
            return ResponseEntity.status(404).body("Candidate not found");
        }

        List<Feedback> feedbackList = feedbackRepository.findByInterview_Candidate_IdOrderByIdDesc(id);
        List<Interview> interviewList = interviewRepository.findByCandidate_Id(id);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("candidate", candidate);
        payload.put("feedbackHistory", feedbackList);
        payload.put("interviewHistory", interviewList);
        payload.put("feedbackCount", feedbackList.size());
        payload.put("latestFeedback", feedbackList.isEmpty() ? null : feedbackList.get(0));
        addRoundProgress(payload, candidate, findCurrentRoundInterview(candidate));

        return ResponseEntity.ok(payload);
    }

    @PostMapping("/panels")
    public ResponseEntity<?> createPanel(@Valid @RequestBody CreatePanelRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("User with this email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(com.interview.tracker.constants.AppConstants.ROLE_PANEL);
        user.setEmailVerified(skipInviteVerification);
        user.setActive(true);
        user.setInvitedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        Panel panel = new Panel();
        panel.setName(request.getName());
        panel.setEmail(request.getEmail());
        panel.setExpertise(request.getExpertise() == null || request.getExpertise().isBlank() ? "General" : request.getExpertise());
        panel.setMobile(request.getPhone());
        panel.setOrganization(request.getOrganization());
        panel.setDesignation(request.getDesignation());
        Panel savedPanel = panelRepository.save(panel);
        log.info("HR onboarded panel profile: email={}", request.getEmail());

        if (skipInviteVerification) {
            emailService.sendPasswordSetEmail(savedUser);
        } else {
            emailService.sendVerificationEmail(savedUser);
        }

        return ResponseEntity.ok(savedPanel);
    }

    @PostMapping("/candidates/{id}/assign-panel")
    public ResponseEntity<?> assignPanelMembers(@PathVariable Long id, @Valid @RequestBody AssignPanelRequest request) {
        Candidate c = candidateRepository.findById(id).orElse(null);
        if (c == null) return ResponseEntity.status(404).body("Candidate not found");

        Stage stage = c.getStage();
        if (stage != Stage.L1_TECH && stage != Stage.L2_TECH && stage != Stage.HR_ROUND) {
            return ResponseEntity.badRequest().body("Panel can be assigned only for L1, L2, or HR round candidates");
        }
        String currentRound = mapStageToInterviewRound(stage);
        if (interviewRepository.existsByCandidate_IdAndRound(id, currentRound)) {
            return ResponseEntity.badRequest().body("Panel already assigned for this round");
        }

        if (request.getInterviewTime() == null) {
            return ResponseEntity.badRequest().body("Interview date and time are required");
        }

        LocalDateTime interviewTime = request.getInterviewTime();
        if (!interviewTime.isAfter(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Interview date and time must be in the future");
        }

        List<String> emails = request.getPanelEmails();
        if (emails == null || emails.isEmpty()) {
            return ResponseEntity.badRequest().body("At least one panel email is required");
        }

        List<String> panels = emails.stream()
                .map(e -> e == null ? null : e.trim())
                .filter(e -> e != null && !e.isBlank())
                .distinct()
                .toList();

        if (panels.size() < 1 || panels.size() > 2) {
            return ResponseEntity.badRequest().body("Panel members must be between 1 and 2");
        }

        List<Panel> panelEntities = panels.stream()
                .map(email -> {
                    User panelUser = userRepository.findByEmail(email)
                            .orElseThrow(() -> new IllegalArgumentException("Panel user account not found for email: " + email));
                    if (!com.interview.tracker.constants.AppConstants.ROLE_PANEL.equals(panelUser.getRole())) {
                        throw new IllegalArgumentException("User is not a PANEL role: " + email);
                    }
                    return panelRepository.findByEmail(email)
                            .orElseThrow(() -> new IllegalArgumentException("Panel profile not found for email: " + email));
                })
                .toList();

        Interview interview = new Interview();
        interview.setCandidate(c);
        interview.setPanels(panelEntities);
        interview.setPanel(panelEntities.get(0));
        interview.setStatus("PENDING");
        interview.setFocusArea(
            (request.getFocusArea() != null && !request.getFocusArea().isBlank()) ? request.getFocusArea() :
            (c.getJd() != null && c.getJd().getSkills() != null && !c.getJd().getSkills().isBlank()) ? c.getJd().getSkills() : "General"
        );
        interview.setRound(currentRound);
        interview.setInterviewTime(interviewTime);
        interview.setDuration(request.getDuration());
        interview.setInterviewType(request.getInterviewType());
        interview.setMeetingLink(blankToNull(request.getMeetingLink()));
        interview.setNotes(blankToNull(request.getNotes()));

        interview.setInterviewerName(panelEntities.stream()
                .map(p -> (p.getName() != null && !p.getName().isBlank()) ? p.getName() : p.getEmail())
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("Panel"));

        Interview saved = interviewRepository.save(interview);

        if (c.getUser() != null) {
            emailService.sendPanelAssignmentEmails(c.getUser(), panelEntities, saved);
        }

        return ResponseEntity.ok(saved);
    }

    private String mapStageToInterviewRound(Stage stage) {
        if (stage == null) return "L1";
        return switch (stage) {
            case PROFILING, SCREENING -> "L1";
            case L1_TECH -> "L1";
            case L2_TECH -> "L2";
            case HR_ROUND -> "HR";
            default -> "L1";
        };
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
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

        ResponseEntity<?> panelRoundGate = validatePanelRoundCanAdvance(c);
        if (panelRoundGate != null) {
            return panelRoundGate;
        }

        Stage next = nextStage(c.getStage());
        if (next == null) return ResponseEntity.badRequest().body("No next stage available");

        c.setStage(next);
        c.setStageStatus(StageStatus.PENDING);
        log.info("Candidate stage advanced: id={}, stage={}", c.getId(), next);

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
        log.info("Candidate rejected by HR: id={}", c.getId());
        Candidate saved = candidateRepository.save(c);
        if (saved.getUser() != null) {
            String jobTitle = (saved.getJd() != null && saved.getJd().getTitle() != null) ? saved.getJd().getTitle() : null;
            emailService.sendCandidateRejectedEmail(saved.getUser(), jobTitle);
        }
        return ResponseEntity.ok(saved);
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
        log.info("Candidate selected by HR: id={}", c.getId());
        Candidate saved = candidateRepository.save(c);
        if (saved.getUser() != null) {
            String jobTitle = (saved.getJd() != null && saved.getJd().getTitle() != null) ? saved.getJd().getTitle() : null;
            emailService.sendCandidateSelectedEmail(saved.getUser(), jobTitle);
        }
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/candidates/{id}")
    public ResponseEntity<?> deleteCandidate(@PathVariable Long id) {
        Candidate c = candidateRepository.findById(id).orElse(null);
        if (c == null) return ResponseEntity.status(404).body("Candidate not found");
        candidateRepository.deleteById(id);
        log.info("Candidate deleted by HR: id={}", id);
        return ResponseEntity.ok("Deleted");
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

    private Interview findCurrentRoundInterview(Candidate candidate) {
        if (candidate == null || candidate.getId() == null || !requiresPanelFeedback(candidate.getStage())) {
            return null;
        }
        return interviewRepository
                .findTopByCandidate_IdAndRoundOrderByInterviewTimeDesc(candidate.getId(), mapStageToInterviewRound(candidate.getStage()))
                .orElse(null);
    }

    private void addRoundProgress(Map<String, Object> item, Candidate candidate, Interview interview) {
        boolean panelRound = candidate != null && requiresPanelFeedback(candidate.getStage());
        int assignedPanelCount = getAssignedPanelCount(interview);
        long receivedFeedbackCount = interview != null && interview.getId() != null
                ? feedbackRepository.countDistinctPanelsByInterviewId(interview.getId())
                : 0L;

        item.put("currentRoundInterview", interview);
        item.put("panelAssignedForCurrentRound", interview != null);
        item.put("canAssignPanel", panelRound && interview == null);
        item.put("assignedPanelCount", assignedPanelCount);
        item.put("currentRoundFeedbackCount", receivedFeedbackCount);
        item.put("canAdvanceStage", !panelRound || (interview != null && assignedPanelCount > 0 && receivedFeedbackCount >= assignedPanelCount));
    }

    private ResponseEntity<?> validatePanelRoundCanAdvance(Candidate candidate) {
        if (!requiresPanelFeedback(candidate.getStage())) {
            return null;
        }

        Interview interview = findCurrentRoundInterview(candidate);
        if (interview == null) {
            return ResponseEntity.badRequest().body("Assign panel for this round before moving to the next stage");
        }

        int assignedPanelCount = getAssignedPanelCount(interview);
        long receivedFeedbackCount = feedbackRepository.countDistinctPanelsByInterviewId(interview.getId());
        if (assignedPanelCount == 0) {
            return ResponseEntity.badRequest().body("Assign panel for this round before moving to the next stage");
        }
        if (receivedFeedbackCount < assignedPanelCount) {
            return ResponseEntity.badRequest().body("All assigned panel members must submit feedback before moving to the next stage");
        }

        return null;
    }

    private boolean requiresPanelFeedback(Stage stage) {
        return stage == Stage.L1_TECH || stage == Stage.L2_TECH || stage == Stage.HR_ROUND;
    }

    private int getAssignedPanelCount(Interview interview) {
        if (interview == null) return 0;
        if (interview.getPanels() != null && !interview.getPanels().isEmpty()) {
            return interview.getPanels().size();
        }
        return interview.getPanel() == null ? 0 : 1;
    }
}

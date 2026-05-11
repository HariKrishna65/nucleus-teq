package com.interview.tracker.controller;

import com.interview.tracker.constants.Stage;
import com.interview.tracker.constants.StageStatus;
import com.interview.tracker.dto.AssignPanelRequest;
import com.interview.tracker.dto.CreatePanelRequest;
import com.interview.tracker.dto.ReferralCandidateRequest;
import com.interview.tracker.entity.Candidate;
import com.interview.tracker.entity.Feedback;
import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.JobDescription;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.CandidateRepository;
import com.interview.tracker.repository.FeedbackRepository;
import com.interview.tracker.repository.InterviewRepository;
import com.interview.tracker.repository.JobDescriptionRepository;
import com.interview.tracker.repository.PanelRepository;
import com.interview.tracker.repository.UserRepository;
import com.interview.tracker.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HrControllerTest {

    @Test
    void getCandidatesWithProgress_returnsCandidateAndFeedbackSummary() {
        CandidateRepository candidateRepository = mock(CandidateRepository.class);
        FeedbackRepository feedbackRepository = mock(FeedbackRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PanelRepository panelRepository = mock(PanelRepository.class);
        EmailService emailService = mock(EmailService.class);
        InterviewRepository interviewRepository = mock(InterviewRepository.class);
        JobDescriptionRepository jobDescriptionRepository = mock(JobDescriptionRepository.class);

        HrController controller = new HrController(
                candidateRepository,
                feedbackRepository,
                userRepository,
                panelRepository,
                emailService,
                interviewRepository,
                jobDescriptionRepository
        );

        Candidate c = new Candidate();
        c.setId(10L);
        c.setStage(Stage.SCREENING);

        Feedback f = new Feedback();
        f.setId(99L);
        f.setComments("Looks good");
        f.setRating(4);

        when(candidateRepository.findAllByOrderByApplicationDateDesc()).thenReturn(List.of(c));
        when(feedbackRepository.findByInterview_Candidate_IdOrderByIdDesc(10L)).thenReturn(List.of(f));

        ResponseEntity<?> response = controller.getCandidatesWithProgress();
        assertEquals(200, response.getStatusCode().value());

        List<?> rows = (List<?>) response.getBody();
        assertNotNull(rows);
        assertEquals(1, rows.size());
        Map<?, ?> row = (Map<?, ?>) rows.get(0);
        assertEquals(c, row.get("candidate"));
        assertEquals(1, row.get("feedbackCount"));
        assertEquals(f, row.get("latestFeedback"));
    }

    @Test
    void getCandidatesWithProgress_normalizesFinalStageStatus() {
        TestContext ctx = context();
        Candidate selected = candidate(12L, Stage.SELECTED);
        selected.setStageStatus(StageStatus.PENDING);
        selected.setStatus("REFERRED");
        when(ctx.candidateRepository.findAllByOrderByApplicationDateDesc()).thenReturn(List.of(selected));
        when(ctx.feedbackRepository.findByInterview_Candidate_IdOrderByIdDesc(12L)).thenReturn(List.of());

        ResponseEntity<?> response = ctx.controller.getCandidatesWithProgress();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(StageStatus.COMPLETED, selected.getStageStatus());
        assertEquals("SELECTED", selected.getStatus());
        verify(ctx.candidateRepository).save(selected);
    }

    @Test
    void reject_withoutComments_returnsBadRequest() {
        CandidateRepository candidateRepository = mock(CandidateRepository.class);
        FeedbackRepository feedbackRepository = mock(FeedbackRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PanelRepository panelRepository = mock(PanelRepository.class);
        EmailService emailService = mock(EmailService.class);
        InterviewRepository interviewRepository = mock(InterviewRepository.class);
        JobDescriptionRepository jobDescriptionRepository = mock(JobDescriptionRepository.class);

        HrController controller = new HrController(
                candidateRepository,
                feedbackRepository,
                userRepository,
                panelRepository,
                emailService,
                interviewRepository,
                jobDescriptionRepository
        );

        Candidate c = new Candidate();
        c.setId(1L);
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(c));

        ResponseEntity<?> response = controller.reject(1L, Map.of());
        assertEquals(400, response.getStatusCode().value());
        assertEquals("HR comments are mandatory for rejection", response.getBody());
    }

    @Test
    void assignPanel_withPastInterviewTime_returnsBadRequest() {
        CandidateRepository candidateRepository = mock(CandidateRepository.class);
        FeedbackRepository feedbackRepository = mock(FeedbackRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PanelRepository panelRepository = mock(PanelRepository.class);
        EmailService emailService = mock(EmailService.class);
        InterviewRepository interviewRepository = mock(InterviewRepository.class);
        JobDescriptionRepository jobDescriptionRepository = mock(JobDescriptionRepository.class);

        HrController controller = new HrController(
                candidateRepository,
                feedbackRepository,
                userRepository,
                panelRepository,
                emailService,
                interviewRepository,
                jobDescriptionRepository
        );

        Candidate c = new Candidate();
        c.setId(1L);
        c.setStage(Stage.L1_TECH);
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(c));

        AssignPanelRequest request = new AssignPanelRequest();
        request.setPanelEmails(List.of("panel@example.com"));
        request.setInterviewTime(LocalDateTime.now().minusMinutes(5));

        ResponseEntity<?> response = controller.assignPanelMembers(1L, request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Interview date and time must be in the future", response.getBody());
        verify(interviewRepository, never()).save(any());
        verify(emailService, never()).sendPanelAssignmentEmails(any(), anyList(), any());
    }

    @Test
    void getCandidateDetails_whenMissing_returnsNotFound() {
        TestContext ctx = context();
        when(ctx.candidateRepository.findById(55L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = ctx.controller.getCandidateDetails(55L);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Candidate not found", response.getBody());
    }

    @Test
    void getCandidateDetails_withPanelRoundAddsProgressFlags() {
        TestContext ctx = context();
        Candidate candidate = candidate(7L, Stage.L1_TECH);
        Feedback feedback = new Feedback();
        Interview interview = new Interview();
        interview.setId(3L);
        interview.setPanels(List.of(new Panel(), new Panel()));

        when(ctx.candidateRepository.findById(7L)).thenReturn(Optional.of(candidate));
        when(ctx.feedbackRepository.findByInterview_Candidate_IdOrderByIdDesc(7L)).thenReturn(List.of(feedback));
        when(ctx.interviewRepository.findByCandidate_Id(7L)).thenReturn(List.of(interview));
        when(ctx.interviewRepository.findTopByCandidate_IdAndRoundOrderByInterviewTimeDesc(7L, "L1"))
                .thenReturn(Optional.of(interview));
        when(ctx.feedbackRepository.countDistinctPanelsByInterviewId(3L)).thenReturn(2L);

        ResponseEntity<?> response = ctx.controller.getCandidateDetails(7L);

        assertEquals(200, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(feedback, body.get("latestFeedback"));
        assertEquals(true, body.get("panelAssignedForCurrentRound"));
        assertEquals(false, body.get("canAssignPanel"));
        assertEquals(true, body.get("canAdvanceStage"));
    }

    @Test
    void createPanel_handlesDuplicateAndInviteModes() {
        TestContext duplicate = context();
        CreatePanelRequest request = panelRequest("panel@example.com");
        when(duplicate.userRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(new User()));

        ResponseEntity<?> duplicateResponse = duplicate.controller.createPanel(request);

        assertEquals(400, duplicateResponse.getStatusCode().value());
        assertEquals("User with this email already exists", duplicateResponse.getBody());

        TestContext skipVerification = context();
        ReflectionTestUtils.setField(skipVerification.controller, "skipInviteVerification", true);
        when(skipVerification.userRepository.findByEmail("panel@example.com")).thenReturn(Optional.empty());
        when(skipVerification.userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(skipVerification.panelRepository.save(any(Panel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> created = skipVerification.controller.createPanel(request);

        assertEquals(200, created.getStatusCode().value());
        verify(skipVerification.emailService).sendPasswordSetEmail(any(User.class));

        TestContext verifyEmail = context();
        ReflectionTestUtils.setField(verifyEmail.controller, "skipInviteVerification", false);
        when(verifyEmail.userRepository.findByEmail("panel@example.com")).thenReturn(Optional.empty());
        when(verifyEmail.userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(verifyEmail.panelRepository.save(any(Panel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        verifyEmail.controller.createPanel(request);

        verify(verifyEmail.emailService).sendVerificationEmail(any(User.class));
    }

    @Test
    void createReferralCandidate_validatesDuplicatesAndCreatesCandidate() {
        ReferralCandidateRequest request = referralRequest();

        TestContext duplicateEmail = context();
        when(duplicateEmail.userRepository.findByEmail("candidate@example.com")).thenReturn(Optional.of(new User()));
        assertEquals(400, duplicateEmail.controller.createReferralCandidate(request).getStatusCode().value());

        TestContext duplicatePhone = context();
        when(duplicatePhone.userRepository.findByEmail("candidate@example.com")).thenReturn(Optional.empty());
        when(duplicatePhone.userRepository.findByPhone("9876543210")).thenReturn(Optional.of(new User()));
        assertEquals(400, duplicatePhone.controller.createReferralCandidate(request).getStatusCode().value());

        TestContext missingJd = context();
        when(missingJd.userRepository.findByEmail("candidate@example.com")).thenReturn(Optional.empty());
        when(missingJd.userRepository.findByPhone("9876543210")).thenReturn(Optional.empty());
        when(missingJd.jobDescriptionRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> missingJd.controller.createReferralCandidate(request));

        TestContext created = context();
        JobDescription jd = new JobDescription();
        jd.setId(2L);
        when(created.userRepository.findByEmail("candidate@example.com")).thenReturn(Optional.empty());
        when(created.userRepository.findByPhone("9876543210")).thenReturn(Optional.empty());
        when(created.jobDescriptionRepository.findById(2L)).thenReturn(Optional.of(jd));
        when(created.userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(created.candidateRepository.save(any(Candidate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = created.controller.createReferralCandidate(request);

        assertEquals(200, response.getStatusCode().value());
        Candidate saved = (Candidate) response.getBody();
        assertEquals(Stage.PROFILING, saved.getStage());
        assertEquals(StageStatus.COMPLETED, saved.getStageStatus());
        assertEquals("Referral", saved.getSource());
        verify(created.emailService).sendPasswordSetEmail(any(User.class));
    }

    @Test
    void assignPanelMembers_coversValidationAndSuccessPaths() {
        AssignPanelRequest request = assignRequest(List.of("panel@example.com"), LocalDateTime.now().plusDays(1));

        TestContext missingCandidate = context();
        when(missingCandidate.candidateRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals(404, missingCandidate.controller.assignPanelMembers(1L, request).getStatusCode().value());

        TestContext wrongStage = context();
        when(wrongStage.candidateRepository.findById(1L)).thenReturn(Optional.of(candidate(1L, Stage.SCREENING)));
        assertEquals(400, wrongStage.controller.assignPanelMembers(1L, request).getStatusCode().value());

        TestContext alreadyAssigned = context();
        when(alreadyAssigned.candidateRepository.findById(1L)).thenReturn(Optional.of(candidate(1L, Stage.L2_TECH)));
        when(alreadyAssigned.interviewRepository.existsByCandidate_IdAndRound(1L, "L2")).thenReturn(true);
        assertEquals(400, alreadyAssigned.controller.assignPanelMembers(1L, request).getStatusCode().value());

        TestContext missingEmails = context();
        when(missingEmails.candidateRepository.findById(1L)).thenReturn(Optional.of(candidate(1L, Stage.L1_TECH)));
        assertEquals(400, missingEmails.controller.assignPanelMembers(1L, assignRequest(null, LocalDateTime.now().plusDays(1))).getStatusCode().value());

        TestContext missingTime = context();
        when(missingTime.candidateRepository.findById(1L)).thenReturn(Optional.of(candidate(1L, Stage.L1_TECH)));
        assertEquals(400, missingTime.controller.assignPanelMembers(1L, assignRequest(List.of("panel@example.com"), null)).getStatusCode().value());

        TestContext tooMany = context();
        when(tooMany.candidateRepository.findById(1L)).thenReturn(Optional.of(candidate(1L, Stage.L1_TECH)));
        assertEquals(400, tooMany.controller.assignPanelMembers(1L,
                assignRequest(List.of("a@example.com", "b@example.com", "c@example.com"), LocalDateTime.now().plusDays(1))).getStatusCode().value());

        TestContext missingPanelUser = context();
        when(missingPanelUser.candidateRepository.findById(1L)).thenReturn(Optional.of(candidate(1L, Stage.L1_TECH)));
        when(missingPanelUser.userRepository.findByEmail("panel@example.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> missingPanelUser.controller.assignPanelMembers(1L, request));

        TestContext wrongRole = context();
        User candidateUserRole = new User();
        candidateUserRole.setRole("CANDIDATE");
        when(wrongRole.candidateRepository.findById(1L)).thenReturn(Optional.of(candidate(1L, Stage.L1_TECH)));
        when(wrongRole.userRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(candidateUserRole));
        assertThrows(IllegalArgumentException.class, () -> wrongRole.controller.assignPanelMembers(1L, request));

        TestContext missingPanelProfile = context();
        User panelUserWithoutProfile = new User();
        panelUserWithoutProfile.setRole("PANEL");
        when(missingPanelProfile.candidateRepository.findById(1L)).thenReturn(Optional.of(candidate(1L, Stage.L1_TECH)));
        when(missingPanelProfile.userRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(panelUserWithoutProfile));
        when(missingPanelProfile.panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> missingPanelProfile.controller.assignPanelMembers(1L, request));

        TestContext success = context();
        Candidate candidate = candidate(1L, Stage.L1_TECH);
        User candidateUser = new User();
        candidateUser.setEmail("candidate@example.com");
        candidate.setUser(candidateUser);
        JobDescription jd = new JobDescription();
        jd.setSkills("Java");
        candidate.setJd(jd);
        User panelUser = new User();
        panelUser.setRole("PANEL");
        Panel panel = new Panel();
        panel.setId(5L);
        panel.setEmail("panel@example.com");
        panel.setName("Panel");
        when(success.candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(success.userRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(panelUser));
        when(success.panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(panel));
        when(success.interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = success.controller.assignPanelMembers(1L, request);

        assertEquals(200, response.getStatusCode().value());
        Interview saved = (Interview) response.getBody();
        assertEquals("Java", saved.getFocusArea());
        assertEquals("Panel", saved.getInterviewerName());
        verify(success.emailService).sendPanelAssignmentEmails(eq(candidateUser), eq(List.of(panel)), eq(saved));

        TestContext twoPanels = context();
        Candidate l2 = candidate(2L, Stage.L2_TECH);
        Panel panelA = panel(11L, "a@example.com", "Panel A");
        Panel panelB = panel(12L, "b@example.com", "Panel B");
        User userA = new User();
        userA.setRole("PANEL");
        User userB = new User();
        userB.setRole("PANEL");
        when(twoPanels.candidateRepository.findById(2L)).thenReturn(Optional.of(l2));
        when(twoPanels.userRepository.findByEmail("a@example.com")).thenReturn(Optional.of(userA));
        when(twoPanels.userRepository.findByEmail("b@example.com")).thenReturn(Optional.of(userB));
        when(twoPanels.panelRepository.findByEmail("a@example.com")).thenReturn(Optional.of(panelA));
        when(twoPanels.panelRepository.findByEmail("b@example.com")).thenReturn(Optional.of(panelB));
        when(twoPanels.interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Interview savedTwoPanel = (Interview) twoPanels.controller.assignPanelMembers(2L,
                assignRequest(List.of("a@example.com", "b@example.com"), LocalDateTime.now().plusDays(1))).getBody();
        assertEquals("L2", savedTwoPanel.getRound());
        assertEquals("Panel A, Panel B", savedTwoPanel.getInterviewerName());
    }

    @Test
    void advanceRejectSelectAndDelete_coverStageTransitions() {
        TestContext missing = context();
        when(missing.candidateRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals(404, missing.controller.advance(1L, null).getStatusCode().value());

        TestContext initial = context();
        Candidate candidate = candidate(1L, null);
        when(initial.candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(initial.candidateRepository.save(candidate)).thenReturn(candidate);
        ResponseEntity<?> advanced = initial.controller.advance(1L, Map.of("comments", "ok"));
        assertEquals(200, advanced.getStatusCode().value());
        assertEquals(Stage.SCREENING, candidate.getStage());
        assertEquals("ok", candidate.getHrComments());

        TestContext finalStage = context();
        when(finalStage.candidateRepository.findById(2L)).thenReturn(Optional.of(candidate(2L, Stage.SELECTED)));
        assertEquals(400, finalStage.controller.advance(2L, null).getStatusCode().value());

        TestContext hrRoundAdvance = context();
        when(hrRoundAdvance.candidateRepository.findById(9L)).thenReturn(Optional.of(candidate(9L, Stage.HR_ROUND)));
        ResponseEntity<?> hrRoundAdvanceResponse = hrRoundAdvance.controller.advance(9L, null);
        assertEquals(400, hrRoundAdvanceResponse.getStatusCode().value());
        assertEquals("Use Select or Reject after the HR round", hrRoundAdvanceResponse.getBody());

        TestContext panelNotAssigned = context();
        when(panelNotAssigned.candidateRepository.findById(3L)).thenReturn(Optional.of(candidate(3L, Stage.L1_TECH)));
        when(panelNotAssigned.interviewRepository.findTopByCandidate_IdAndRoundOrderByInterviewTimeDesc(3L, "L1"))
                .thenReturn(Optional.empty());
        assertEquals(400, panelNotAssigned.controller.advance(3L, null).getStatusCode().value());

        TestContext panelFeedbackComplete = context();
        Candidate l1 = candidate(4L, Stage.L1_TECH);
        Interview interview = new Interview();
        interview.setId(9L);
        interview.setPanel(new Panel());
        when(panelFeedbackComplete.candidateRepository.findById(4L)).thenReturn(Optional.of(l1));
        when(panelFeedbackComplete.interviewRepository.findTopByCandidate_IdAndRoundOrderByInterviewTimeDesc(4L, "L1"))
                .thenReturn(Optional.of(interview));
        when(panelFeedbackComplete.feedbackRepository.countDistinctPanelsByInterviewId(9L)).thenReturn(1L);
        when(panelFeedbackComplete.candidateRepository.save(l1)).thenReturn(l1);
        assertEquals(200, panelFeedbackComplete.controller.advance(4L, null).getStatusCode().value());
        assertEquals(Stage.L2_TECH, l1.getStage());

        TestContext reject = context();
        Candidate rejected = candidate(5L, Stage.HR_ROUND);
        User user = new User();
        user.setEmail("candidate@example.com");
        rejected.setUser(user);
        JobDescription jd = new JobDescription();
        jd.setTitle("Java Developer");
        rejected.setJd(jd);
        when(reject.candidateRepository.findById(5L)).thenReturn(Optional.of(rejected));
        when(reject.candidateRepository.save(rejected)).thenReturn(rejected);
        assertEquals(200, reject.controller.reject(5L, Map.of("comments", "no")).getStatusCode().value());
        verify(reject.emailService).sendCandidateRejectedEmail(user, "Java Developer");

        TestContext select = context();
        Candidate selected = candidate(6L, Stage.HR_ROUND);
        User selectedUser = new User();
        selectedUser.setEmail("selected@example.com");
        selected.setUser(selectedUser);
        when(select.candidateRepository.findById(6L)).thenReturn(Optional.of(selected));
        when(select.candidateRepository.save(selected)).thenReturn(selected);
        assertEquals(400, select.controller.select(6L, Map.of()).getStatusCode().value());
        assertEquals(200, select.controller.select(6L, Map.of("comments", "yes")).getStatusCode().value());
        verify(select.emailService).sendCandidateSelectedEmail(selectedUser, null);

        TestContext selectTooEarly = context();
        when(selectTooEarly.candidateRepository.findById(10L)).thenReturn(Optional.of(candidate(10L, Stage.L2_TECH)));
        ResponseEntity<?> selectTooEarlyResponse = selectTooEarly.controller.select(10L, Map.of("comments", "yes"));
        assertEquals(400, selectTooEarlyResponse.getStatusCode().value());
        assertEquals("Candidate can be selected only after the HR round", selectTooEarlyResponse.getBody());

        TestContext moreFeedbackNeeded = context();
        Candidate l2Candidate = candidate(8L, Stage.L2_TECH);
        Interview l2Interview = new Interview();
        l2Interview.setId(10L);
        l2Interview.setPanels(List.of(new Panel(), new Panel()));
        when(moreFeedbackNeeded.candidateRepository.findById(8L)).thenReturn(Optional.of(l2Candidate));
        when(moreFeedbackNeeded.interviewRepository.findTopByCandidate_IdAndRoundOrderByInterviewTimeDesc(8L, "L2"))
                .thenReturn(Optional.of(l2Interview));
        when(moreFeedbackNeeded.feedbackRepository.countDistinctPanelsByInterviewId(10L)).thenReturn(1L);
        assertEquals(400, moreFeedbackNeeded.controller.advance(8L, null).getStatusCode().value());

        TestContext delete = context();
        when(delete.candidateRepository.findById(7L)).thenReturn(Optional.of(candidate(7L, Stage.SCREENING)));
        assertEquals(200, delete.controller.deleteCandidate(7L).getStatusCode().value());
        verify(delete.candidateRepository).deleteById(7L);
    }

    private TestContext context() {
        CandidateRepository candidateRepository = mock(CandidateRepository.class);
        FeedbackRepository feedbackRepository = mock(FeedbackRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PanelRepository panelRepository = mock(PanelRepository.class);
        EmailService emailService = mock(EmailService.class);
        InterviewRepository interviewRepository = mock(InterviewRepository.class);
        JobDescriptionRepository jobDescriptionRepository = mock(JobDescriptionRepository.class);
        HrController controller = new HrController(
                candidateRepository,
                feedbackRepository,
                userRepository,
                panelRepository,
                emailService,
                interviewRepository,
                jobDescriptionRepository
        );
        return new TestContext(
                controller,
                candidateRepository,
                feedbackRepository,
                userRepository,
                panelRepository,
                emailService,
                interviewRepository,
                jobDescriptionRepository
        );
    }

    private Candidate candidate(Long id, Stage stage) {
        Candidate candidate = new Candidate();
        candidate.setId(id);
        candidate.setStage(stage);
        return candidate;
    }

    private CreatePanelRequest panelRequest(String email) {
        CreatePanelRequest request = new CreatePanelRequest();
        request.setName("Panel User");
        request.setEmail(email);
        request.setPhone("9876543210");
        request.setOrganization("NucleusTeq");
        request.setDesignation("Engineer");
        request.setExpertise(" ");
        return request;
    }

    private ReferralCandidateRequest referralRequest() {
        ReferralCandidateRequest request = new ReferralCandidateRequest();
        request.setName("Candidate");
        request.setEmail("candidate@example.com");
        request.setPhone("9876543210");
        request.setJdId(2L);
        request.setExperience(null);
        request.setSource(" ");
        return request;
    }

    private AssignPanelRequest assignRequest(List<String> emails, LocalDateTime interviewTime) {
        AssignPanelRequest request = new AssignPanelRequest();
        request.setPanelEmails(emails);
        request.setInterviewTime(interviewTime);
        request.setDuration(45);
        request.setInterviewType("Online");
        request.setMeetingLink(" ");
        request.setNotes(" ");
        return request;
    }

    private Panel panel(Long id, String email, String name) {
        Panel panel = new Panel();
        panel.setId(id);
        panel.setEmail(email);
        panel.setName(name);
        return panel;
    }

    private record TestContext(
            HrController controller,
            CandidateRepository candidateRepository,
            FeedbackRepository feedbackRepository,
            UserRepository userRepository,
            PanelRepository panelRepository,
            EmailService emailService,
            InterviewRepository interviewRepository,
            JobDescriptionRepository jobDescriptionRepository
    ) {}
}

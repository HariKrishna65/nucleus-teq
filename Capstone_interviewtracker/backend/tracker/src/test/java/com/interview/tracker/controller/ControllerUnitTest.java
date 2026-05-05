package com.interview.tracker.controller;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.entity.Feedback;
import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.JobDescription;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.PanelRepository;
import com.interview.tracker.dto.AuthResponse;
import com.interview.tracker.dto.CreateTestUserRequest;
import com.interview.tracker.dto.EmailRequest;
import com.interview.tracker.dto.LoginRequest;
import com.interview.tracker.dto.RegisterRequest;
import com.interview.tracker.dto.SetPasswordRequest;
import com.interview.tracker.dto.VerifyEmailRequest;
import com.interview.tracker.service.CandidateService;
import com.interview.tracker.service.FeedbackService;
import com.interview.tracker.service.InterviewService;
import com.interview.tracker.service.JobDescriptionService;
import com.interview.tracker.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControllerUnitTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void candidateDownloadResume_whenResumeExists_returnsPdfResponse() {
        CandidateService service = mock(CandidateService.class);
        Candidate candidate = new Candidate();
        candidate.setResumeFileName("resume.pdf");
        candidate.setResumeData("pdf".getBytes());
        when(service.getCandidateById(1L)).thenReturn(candidate);

        ResponseEntity<byte[]> response = new CandidateController(service).downloadResume(1L);

        assertEquals(200, response.getStatusCode().value());
        assertArrayEquals("pdf".getBytes(), response.getBody());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertEquals("attachment; filename=\"resume.pdf\"", response.getHeaders().getFirst("Content-Disposition"));
    }

    @Test
    void candidateDownloadResume_withoutResume_returnsNotFound() {
        CandidateService service = mock(CandidateService.class);
        when(service.getCandidateById(1L)).thenReturn(new Candidate());

        ResponseEntity<byte[]> response = new CandidateController(service).downloadResume(1L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void feedbackController_submitAndLatest_delegateToService() {
        FeedbackService service = mock(FeedbackService.class);
        Feedback feedback = new Feedback();
        when(service.save(feedback)).thenReturn(feedback);
        when(service.getLatestByInterview(2L)).thenReturn(feedback);
        FeedbackController controller = new FeedbackController(service);

        assertEquals(feedback, controller.submit(feedback).getBody());
        assertEquals(feedback, controller.getLatestByInterview(2L).getBody());
    }

    @Test
    void interviewGetById_asPanelNotAssigned_returnsForbidden() {
        InterviewService interviewService = mock(InterviewService.class);
        PanelRepository panelRepository = mock(PanelRepository.class);
        Interview interview = new Interview();
        Panel assigned = new Panel();
        assigned.setId(1L);
        interview.setPanel(assigned);

        setJwt("panel@example.com", 9L, "PANEL");
        Panel currentPanel = new Panel();
        currentPanel.setId(2L);
        when(interviewService.getById(3L)).thenReturn(interview);
        when(panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(currentPanel));

        ResponseEntity<?> response = new InterviewController(interviewService, panelRepository).getById(3L);

        assertEquals(403, response.getStatusCode().value());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    void interviewGetByPanel_asHr_usesRequestedPanelId() {
        InterviewService interviewService = mock(InterviewService.class);
        PanelRepository panelRepository = mock(PanelRepository.class);
        Interview interview = new Interview();
        setJwt("hr@example.com", 1L, "HR");
        when(interviewService.getByPanel(4L)).thenReturn(List.of(interview));

        ResponseEntity<?> response = new InterviewController(interviewService, panelRepository).getByPanel(4L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(interview), response.getBody());
    }

    @Test
    void interviewController_coversCandidatePanelAndDeniedBranches() {
        InterviewService interviewService = mock(InterviewService.class);
        PanelRepository panelRepository = mock(PanelRepository.class);
        InterviewController controller = new InterviewController(interviewService, panelRepository);

        when(interviewService.getById(99L)).thenReturn(null);
        assertEquals(404, controller.getById(99L).getStatusCode().value());

        Candidate candidate = new Candidate();
        User user = new User();
        user.setId(7L);
        candidate.setUser(user);
        Interview candidateInterview = new Interview();
        candidateInterview.setCandidate(candidate);
        when(interviewService.getById(7L)).thenReturn(candidateInterview);
        setJwt("candidate@example.com", 8L, "CANDIDATE");
        assertEquals(403, controller.getById(7L).getStatusCode().value());

        setJwt("candidate@example.com", 7L, "CANDIDATE");
        assertEquals(200, controller.getById(7L).getStatusCode().value());

        Panel assigned = new Panel();
        assigned.setId(5L);
        Interview panelInterview = new Interview();
        panelInterview.setPanels(List.of(assigned));
        Panel current = new Panel();
        current.setId(5L);
        when(interviewService.getById(5L)).thenReturn(panelInterview);
        when(panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(current));
        setJwt("panel@example.com", 3L, "PANEL");
        assertEquals(200, controller.getById(5L).getStatusCode().value());

        when(panelRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        setJwt("missing@example.com", 3L, "PANEL");
        assertEquals(403, controller.getById(5L).getStatusCode().value());

        setJwt("candidate@example.com", 3L, "CANDIDATE");
        assertEquals(403, controller.getByPanel(5L).getStatusCode().value());

        setJwt("panel@example.com", 3L, "PANEL");
        when(panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(current));
        when(interviewService.getByPanel(5L)).thenReturn(List.of(panelInterview));
        assertEquals(List.of(panelInterview), controller.getByPanel(1L).getBody());

        Panel newPanel = new Panel();
        when(panelRepository.save(newPanel)).thenReturn(newPanel);
        when(panelRepository.findAll()).thenReturn(List.of(newPanel));
        assertEquals(newPanel, controller.createPanel(newPanel));
        assertEquals(List.of(newPanel), controller.getPanels());

        when(interviewService.scheduleInterview(panelInterview)).thenReturn(panelInterview);
        assertEquals(panelInterview, controller.scheduleInterview(panelInterview));
    }

    @Test
    void jobDescriptionGetById_whenMissing_returnsNotFound() {
        JobDescriptionService service = mock(JobDescriptionService.class);
        when(service.getById(88L)).thenReturn(null);

        ResponseEntity<?> response = new JobDescriptionController(service).getById(88L);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("JD not found", response.getBody());
    }

    @Test
    void jobDescriptionCreateGetAllAndDelete_delegateToService() {
        JobDescriptionService service = mock(JobDescriptionService.class);
        JobDescription jd = new JobDescription();
        when(service.save(jd)).thenReturn(jd);
        when(service.getAll()).thenReturn(List.of(jd));
        JobDescriptionController controller = new JobDescriptionController(service);

        assertEquals(jd, controller.createJD(jd).getBody());
        assertEquals(List.of(jd), controller.getAll());
        assertEquals("Deleted successfully", controller.delete(2L).getBody());
        verify(service).delete(2L);
    }

    @Test
    void authController_delegatesAllEndpointsToUserService() {
        UserService service = mock(UserService.class);
        AuthController controller = new AuthController(service);
        AuthResponse response = new AuthResponse("ok", "HR", 1L);
        RegisterRequest register = new RegisterRequest();
        register.setEmail("hr@example.com");
        register.setRole("HR");
        LoginRequest login = new LoginRequest();
        login.setEmail("hr@example.com");
        VerifyEmailRequest verify = new VerifyEmailRequest();
        verify.setToken("token");
        SetPasswordRequest setPassword = new SetPasswordRequest();
        EmailRequest email = new EmailRequest();
        email.setEmail("hr@example.com");
        CreateTestUserRequest testUser = new CreateTestUserRequest();

        when(service.register(register)).thenReturn(response);
        when(service.login(login)).thenReturn(response);
        when(service.verifyEmail("token")).thenReturn(response);
        when(service.verifyAndPreparePasswordSetup("token")).thenReturn(Map.of("message", "ready"));
        when(service.setPassword(setPassword)).thenReturn(response);
        when(service.requestPasswordReset("hr@example.com")).thenReturn(response);
        when(service.createTestUser(testUser)).thenReturn(response);
        when(service.resendVerificationEmail("hr@example.com")).thenReturn(response);

        assertEquals(response, controller.register(register));
        assertEquals(response, controller.login(login));
        assertEquals(response, controller.verifyEmail(verify));
        assertEquals("ready", controller.verifyAndSetPassword(verify).get("message"));
        assertEquals(response, controller.setPassword(setPassword));
        assertEquals(response, controller.forgotPassword(email));
        assertEquals(response, controller.createTestUser(testUser));
        assertEquals(response, controller.resendVerificationEmail(email));
    }

    private void setJwt(String subject, Long userId, String role) {
        Jwt jwt = new Jwt("token", null, null, Map.of("alg", "none"), Map.of(
                "sub", subject,
                "userId", userId,
                "role", role
        ));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}

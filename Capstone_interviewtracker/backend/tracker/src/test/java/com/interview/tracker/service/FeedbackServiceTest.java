package com.interview.tracker.service;

import com.interview.tracker.entity.Feedback;
import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.repository.FeedbackRepository;
import com.interview.tracker.repository.InterviewRepository;
import com.interview.tracker.repository.PanelRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private InterviewRepository interviewRepository;
    @Mock
    private PanelRepository panelRepository;

    private FeedbackService service;

    @BeforeEach
    void setUp() {
        service = new FeedbackService(feedbackRepository, interviewRepository, panelRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void save_asAssignedPanel_setsPanelAndCompletesInterviewWhenAllSubmitted() {
        setJwt("panel@example.com", "PANEL");
        Panel panel = panel(8L, "panel@example.com");
        Interview interview = new Interview();
        interview.setId(3L);
        interview.setInterviewTime(LocalDateTime.now().minusMinutes(5));
        interview.setPanels(List.of(panel));
        Feedback feedback = validFeedback(3L);

        when(interviewRepository.findById(3L)).thenReturn(Optional.of(interview));
        when(panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(panel));
        when(feedbackRepository.existsByInterview_IdAndPanel_Id(3L, 8L)).thenReturn(false);
        when(feedbackRepository.save(feedback)).thenReturn(feedback);
        when(feedbackRepository.countDistinctPanelsByInterviewId(3L)).thenReturn(1L);

        Feedback saved = service.save(feedback);

        assertEquals(panel, saved.getPanel());
        assertEquals("COMPLETED", interview.getStatus());
        verify(interviewRepository).save(interview);
    }

    @Test
    void save_asPanelNotAssigned_rejects() {
        setJwt("panel@example.com", "PANEL");
        Interview interview = new Interview();
        interview.setId(3L);
        interview.setPanels(List.of(panel(9L, "other@example.com")));

        when(interviewRepository.findById(3L)).thenReturn(Optional.of(interview));
        when(panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(panel(8L, "panel@example.com")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.save(validFeedback(3L)));

        assertEquals("You are not assigned to this interview", ex.getMessage());
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void save_withInvalidRating_rejectsBeforeRepositoryLookup() {
        Feedback feedback = validFeedback(3L);
        feedback.setRating(6);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(feedback));

        assertEquals("Rating must be between 1 and 5", ex.getMessage());
        verifyNoInteractions(interviewRepository);
    }

    @Test
    void save_withNullAndLowRating_rejectsBeforeRepositoryLookup() {
        Feedback nullRating = validFeedback(3L);
        nullRating.setRating(null);
        assertEquals("Rating must be between 1 and 5", assertThrows(IllegalArgumentException.class,
                () -> service.save(nullRating)).getMessage());

        Feedback lowRating = validFeedback(3L);
        lowRating.setRating(0);
        assertEquals("Rating must be between 1 and 5", assertThrows(IllegalArgumentException.class,
                () -> service.save(lowRating)).getMessage());

        verifyNoInteractions(interviewRepository);
    }

    @Test
    void getLatestByInterview_asHr_returnsLatestOrNull() {
        setJwt("hr@example.com", "HR");
        Interview interview = new Interview();
        Feedback latest = validFeedback(3L);
        when(interviewRepository.findById(3L)).thenReturn(Optional.of(interview));
        when(feedbackRepository.findTopByInterview_IdOrderByIdDesc(3L)).thenReturn(Optional.of(latest));

        assertEquals(latest, service.getLatestByInterview(3L));
    }

    @Test
    void getLatestByInterview_asHrReturnsNullWhenNoFeedbackExists() {
        setJwt("hr@example.com", "HR");
        when(interviewRepository.findById(3L)).thenReturn(Optional.of(new Interview()));
        when(feedbackRepository.findTopByInterview_IdOrderByIdDesc(3L)).thenReturn(Optional.empty());

        assertNull(service.getLatestByInterview(3L));
    }

    @Test
    void save_validatesRequiredFieldsBeforeRepositoryLookup() {
        Feedback missingInterview = new Feedback();
        assertEquals("Interview is required", assertThrows(IllegalArgumentException.class,
                () -> service.save(missingInterview)).getMessage());

        Feedback missingComments = validFeedback(3L);
        missingComments.setComments(" ");
        assertEquals("Comments are mandatory", assertThrows(IllegalArgumentException.class,
                () -> service.save(missingComments)).getMessage());

        Feedback missingStatus = validFeedback(3L);
        missingStatus.setStatus(" ");
        assertEquals("Status is required (Selected/Rejected)", assertThrows(IllegalArgumentException.class,
                () -> service.save(missingStatus)).getMessage());

        verifyNoInteractions(interviewRepository);
    }

    @Test
    void save_withInterviewIdMissing_rejectsBeforeRepositoryLookup() {
        Feedback feedback = validFeedback(null);

        assertEquals("Interview is required", assertThrows(IllegalArgumentException.class,
                () -> service.save(feedback)).getMessage());
        verifyNoInteractions(interviewRepository);
    }

    @Test
    void save_asHrAllowsFeedbackAndLeavesPendingWhenMorePanelsRemain() {
        setJwt("hr@example.com", "HR");
        Panel panel1 = panel(1L, "one@example.com");
        Panel panel2 = panel(2L, "two@example.com");
        Interview interview = new Interview();
        interview.setId(3L);
        interview.setInterviewTime(LocalDateTime.now().minusMinutes(5));
        interview.setPanels(List.of(panel1, panel2));
        Feedback feedback = validFeedback(3L);

        when(interviewRepository.findById(3L)).thenReturn(Optional.of(interview));
        when(feedbackRepository.save(feedback)).thenReturn(feedback);
        when(feedbackRepository.countDistinctPanelsByInterviewId(3L)).thenReturn(1L);

        Feedback saved = service.save(feedback);

        assertEquals(feedback, saved);
        assertEquals("FEEDBACK_PENDING", interview.getStatus());
        verify(panelRepository, never()).findByEmail(any());
    }

    @Test
    void save_asHrCompletesInterviewWhenSinglePanelFallbackIsCovered() {
        setJwt("hr@example.com", "HR");
        Panel panel = panel(1L, "one@example.com");
        Interview interview = new Interview();
        interview.setId(3L);
        interview.setInterviewTime(LocalDateTime.now().minusMinutes(5));
        interview.setPanel(panel);
        Feedback feedback = validFeedback(3L);

        when(interviewRepository.findById(3L)).thenReturn(Optional.of(interview));
        when(feedbackRepository.save(feedback)).thenReturn(feedback);
        when(feedbackRepository.countDistinctPanelsByInterviewId(3L)).thenReturn(1L);

        service.save(feedback);

        assertEquals("COMPLETED", interview.getStatus());
    }

    @Test
    void save_rejectsUnknownRoleAndDuplicatePanelFeedback() {
        setJwt("candidate@example.com", "CANDIDATE");
        when(interviewRepository.findById(3L)).thenReturn(Optional.of(new Interview()));

        assertEquals("Access denied", assertThrows(IllegalArgumentException.class,
                () -> service.save(validFeedback(3L))).getMessage());

        tearDown();
        reset(feedbackRepository, interviewRepository, panelRepository);
        setUp();
        setJwt("panel@example.com", "PANEL");
        Panel panel = panel(8L, "panel@example.com");
        Interview interview = new Interview();
        interview.setId(3L);
        interview.setPanel(panel);
        when(interviewRepository.findById(3L)).thenReturn(Optional.of(interview));
        when(panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(panel));
        when(feedbackRepository.existsByInterview_IdAndPanel_Id(3L, 8L)).thenReturn(true);

        assertEquals("Feedback already submitted for this interview", assertThrows(IllegalArgumentException.class,
                () -> service.save(validFeedback(3L))).getMessage());
    }

    @Test
    void save_beforeInterviewTime_rejectsFeedback() {
        setJwt("hr@example.com", "HR");
        Interview interview = new Interview();
        interview.setId(3L);
        interview.setInterviewTime(LocalDateTime.now().plusMinutes(30));
        Feedback feedback = validFeedback(3L);

        when(interviewRepository.findById(3L)).thenReturn(Optional.of(interview));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(feedback));

        assertEquals("Feedback can be submitted only after the interview time", ex.getMessage());
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void getLatestByInterview_coversPanelAndDeniedBranches() {
        setJwt("panel@example.com", "PANEL");
        Panel panel = panel(8L, "panel@example.com");
        Interview assigned = new Interview();
        assigned.setPanel(panel);
        Feedback latest = validFeedback(3L);
        when(interviewRepository.findById(3L)).thenReturn(Optional.of(assigned));
        when(panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(panel));
        when(feedbackRepository.findTopByInterview_IdOrderByIdDesc(3L)).thenReturn(Optional.of(latest));

        assertEquals(latest, service.getLatestByInterview(3L));

        tearDown();
        reset(feedbackRepository, interviewRepository, panelRepository);
        setUp();
        setJwt("panel@example.com", "PANEL");
        Interview notAssigned = new Interview();
        notAssigned.setPanel(panel(9L, "other@example.com"));
        when(interviewRepository.findById(4L)).thenReturn(Optional.of(notAssigned));
        when(panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(panel));
        assertEquals("You are not assigned to this interview", assertThrows(IllegalArgumentException.class,
                () -> service.getLatestByInterview(4L)).getMessage());

        tearDown();
        reset(feedbackRepository, interviewRepository, panelRepository);
        setUp();
        setJwt("candidate@example.com", "CANDIDATE");
        when(interviewRepository.findById(5L)).thenReturn(Optional.of(new Interview()));
        assertEquals("Access denied", assertThrows(IllegalArgumentException.class,
                () -> service.getLatestByInterview(5L)).getMessage());
    }

    @Test
    void getLatestByInterview_asPanelAssignedThroughPanelListReturnsLatest() {
        setJwt("panel@example.com", "PANEL");
        Panel panel = panel(8L, "panel@example.com");
        Interview assigned = new Interview();
        assigned.setPanels(List.of(panel));
        Feedback latest = validFeedback(3L);

        when(interviewRepository.findById(3L)).thenReturn(Optional.of(assigned));
        when(panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(panel));
        when(feedbackRepository.findTopByInterview_IdOrderByIdDesc(3L)).thenReturn(Optional.of(latest));

        assertEquals(latest, service.getLatestByInterview(3L));
    }

    @Test
    void repositoryMissingBranchesReturnExpectedErrors() {
        setJwt("hr@example.com", "HR");
        when(interviewRepository.findById(3L)).thenReturn(Optional.empty());
        assertEquals("Interview not found", assertThrows(IllegalArgumentException.class,
                () -> service.save(validFeedback(3L))).getMessage());

        tearDown();
        reset(feedbackRepository, interviewRepository, panelRepository);
        setUp();
        setJwt("panel@example.com", "PANEL");
        when(interviewRepository.findById(3L)).thenReturn(Optional.of(new Interview()));
        when(panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.empty());
        assertEquals("Panel profile not found", assertThrows(IllegalArgumentException.class,
                () -> service.save(validFeedback(3L))).getMessage());

        tearDown();
        reset(feedbackRepository, interviewRepository, panelRepository);
        setUp();
        setJwt("hr@example.com", "HR");
        when(interviewRepository.findById(4L)).thenReturn(Optional.empty());
        assertEquals("Interview not found", assertThrows(IllegalArgumentException.class,
                () -> service.getLatestByInterview(4L)).getMessage());

        tearDown();
        reset(feedbackRepository, interviewRepository, panelRepository);
        setUp();
        setJwt("panel@example.com", "PANEL");
        when(interviewRepository.findById(5L)).thenReturn(Optional.of(new Interview()));
        when(panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.empty());
        assertEquals("Panel profile not found", assertThrows(IllegalArgumentException.class,
                () -> service.getLatestByInterview(5L)).getMessage());
    }

    private Feedback validFeedback(Long interviewId) {
        Interview interview = new Interview();
        interview.setId(interviewId);
        Feedback feedback = new Feedback();
        feedback.setInterview(interview);
        feedback.setComments("Good communication");
        feedback.setRating(4);
        feedback.setStatus("Selected");
        return feedback;
    }

    private Panel panel(Long id, String email) {
        Panel panel = new Panel();
        panel.setId(id);
        panel.setEmail(email);
        return panel;
    }

    private void setJwt(String subject, String role) {
        Jwt jwt = new Jwt("token", null, null, Map.of("alg", "none"), Map.of("sub", subject, "role", role));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}

package com.interview.tracker.controller;

import com.interview.tracker.constants.Stage;
import com.interview.tracker.dto.AssignPanelRequest;
import com.interview.tracker.entity.Candidate;
import com.interview.tracker.entity.Feedback;
import com.interview.tracker.repository.CandidateRepository;
import com.interview.tracker.repository.FeedbackRepository;
import com.interview.tracker.repository.InterviewRepository;
import com.interview.tracker.repository.PanelRepository;
import com.interview.tracker.repository.UserRepository;
import com.interview.tracker.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

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

        HrController controller = new HrController(
                candidateRepository,
                feedbackRepository,
                userRepository,
                panelRepository,
                emailService,
                interviewRepository
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
    void reject_withoutComments_returnsBadRequest() {
        CandidateRepository candidateRepository = mock(CandidateRepository.class);
        FeedbackRepository feedbackRepository = mock(FeedbackRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PanelRepository panelRepository = mock(PanelRepository.class);
        EmailService emailService = mock(EmailService.class);
        InterviewRepository interviewRepository = mock(InterviewRepository.class);

        HrController controller = new HrController(
                candidateRepository,
                feedbackRepository,
                userRepository,
                panelRepository,
                emailService,
                interviewRepository
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

        HrController controller = new HrController(
                candidateRepository,
                feedbackRepository,
                userRepository,
                panelRepository,
                emailService,
                interviewRepository
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
}

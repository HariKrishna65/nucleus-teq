package com.interview.tracker.service;

import com.interview.tracker.constants.Stage;
import com.interview.tracker.entity.Candidate;
import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.repository.InterviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InterviewServiceTest {

    private InterviewRepository interviewRepository;
    private InterviewService service;

    @BeforeEach
    void setUp() {
        interviewRepository = mock(InterviewRepository.class);
        service = new InterviewService(interviewRepository);
    }

    @Test
    void scheduleInterview_withSinglePanel_setsPanelNameStatusAndRound() {
        Panel panel = new Panel();
        panel.setId(1L);
        panel.setName("Priya");
        Candidate candidate = new Candidate();
        candidate.setStage(Stage.L2_TECH);
        Interview interview = new Interview();
        interview.setPanels(List.of(panel));
        interview.setCandidate(candidate);
        when(interviewRepository.save(interview)).thenReturn(interview);

        Interview saved = service.scheduleInterview(interview);

        assertEquals(panel, saved.getPanel());
        assertEquals("Priya", saved.getInterviewerName());
        assertEquals("PENDING", saved.getStatus());
        assertEquals("L2", saved.getRound());
    }

    @Test
    void scheduleInterview_withMoreThanTwoPanels_rejects() {
        Interview interview = new Interview();
        interview.setPanels(List.of(new Panel(), new Panel(), new Panel()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.scheduleInterview(interview));

        assertEquals("Maximum 2 panel members allowed per interview", ex.getMessage());
        verify(interviewRepository, never()).save(any());
    }

    @Test
    void getByPanelAndGetById_delegateToRepository() {
        Interview interview = new Interview();
        when(interviewRepository.findAssignedToPanel(4L)).thenReturn(List.of(interview));
        when(interviewRepository.findById(3L)).thenReturn(Optional.of(interview));

        assertEquals(List.of(interview), service.getByPanel(4L));
        assertEquals(interview, service.getById(3L));
    }

    @Test
    void scheduleInterview_mapsRoundsAndBuildsFallbackInterviewerNames() {
        Panel emailOnly = new Panel();
        emailOnly.setEmail("panel@example.com");
        Candidate hrCandidate = new Candidate();
        hrCandidate.setStage(Stage.HR_ROUND);
        Interview hr = new Interview();
        hr.setPanel(emailOnly);
        hr.setCandidate(hrCandidate);
        when(interviewRepository.save(hr)).thenReturn(hr);

        Interview savedHr = service.scheduleInterview(hr);

        assertEquals("panel@example.com", savedHr.getInterviewerName());
        assertEquals("HR", savedHr.getRound());

        Candidate screeningCandidate = new Candidate();
        screeningCandidate.setStage(Stage.SCREENING);
        Interview screening = new Interview();
        screening.setPanels(List.of(emailOnly, emailOnly));
        screening.setCandidate(screeningCandidate);
        when(interviewRepository.save(screening)).thenReturn(screening);

        Interview savedScreening = service.scheduleInterview(screening);

        assertEquals("panel@example.com", savedScreening.getInterviewerName());
        assertEquals("L1", savedScreening.getRound());

        Interview noPanel = new Interview();
        when(interviewRepository.save(noPanel)).thenReturn(noPanel);

        Interview savedNoPanel = service.scheduleInterview(noPanel);

        assertEquals("Panel", savedNoPanel.getInterviewerName());
        assertEquals("L1", savedNoPanel.getRound());

        Candidate l1Candidate = new Candidate();
        l1Candidate.setStage(Stage.L1_TECH);
        Interview l1 = new Interview();
        l1.setCandidate(l1Candidate);
        when(interviewRepository.save(l1)).thenReturn(l1);
        assertEquals("L1", service.scheduleInterview(l1).getRound());
    }

    @Test
    void scheduleInterview_keepsExistingFieldsWhenProvided() {
        Interview interview = new Interview();
        interview.setInterviewerName("Existing");
        interview.setStatus("SCHEDULED");
        interview.setRound("CUSTOM");
        when(interviewRepository.save(interview)).thenReturn(interview);

        Interview saved = service.scheduleInterview(interview);

        assertEquals("Existing", saved.getInterviewerName());
        assertEquals("SCHEDULED", saved.getStatus());
        assertEquals("CUSTOM", saved.getRound());
    }

    @Test
    void scheduleInterview_handlesBlankPanelNamesAndExistingPanel() {
        Panel blankNamed = new Panel();
        blankNamed.setName(" ");
        blankNamed.setEmail("blank@example.com");
        Panel named = new Panel();
        named.setName("Second Panel");
        named.setEmail("second@example.com");
        Panel existing = new Panel();
        existing.setName("Existing Panel");

        Interview interview = new Interview();
        interview.setPanel(existing);
        interview.setPanels(List.of(blankNamed, named));
        when(interviewRepository.save(interview)).thenReturn(interview);

        Interview saved = service.scheduleInterview(interview);

        assertEquals(existing, saved.getPanel());
        assertEquals("blank@example.com, Second Panel", saved.getInterviewerName());
        assertEquals("PENDING", saved.getStatus());
    }

    @Test
    void scheduleInterview_usesPanelNameFallbacksWhenPanelListHasNoUsableText() {
        Panel blank = new Panel();
        blank.setName(" ");
        blank.setEmail(" ");
        Panel fallback = new Panel();
        fallback.setName("Fallback Panel");

        Interview interview = new Interview();
        interview.setPanels(List.of(blank));
        interview.setPanel(fallback);
        when(interviewRepository.save(interview)).thenReturn(interview);

        Interview saved = service.scheduleInterview(interview);

        assertEquals("Fallback Panel", saved.getInterviewerName());

        Panel fallbackEmail = new Panel();
        fallbackEmail.setName(" ");
        fallbackEmail.setEmail("fallback@example.com");
        Interview emailFallback = new Interview();
        emailFallback.setPanels(List.of(blank));
        emailFallback.setPanel(fallbackEmail);
        when(interviewRepository.save(emailFallback)).thenReturn(emailFallback);

        assertEquals("fallback@example.com", service.scheduleInterview(emailFallback).getInterviewerName());
    }

    @Test
    void getById_whenMissing_returnsNull() {
        when(interviewRepository.findById(404L)).thenReturn(Optional.empty());

        assertNull(service.getById(404L));
    }
}

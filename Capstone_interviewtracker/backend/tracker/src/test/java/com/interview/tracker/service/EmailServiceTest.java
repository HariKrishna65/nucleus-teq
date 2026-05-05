package com.interview.tracker.service;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.JobDescription;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private UserRepository userRepository;
    private EmailService service;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        userRepository = mock(UserRepository.class);
        service = new EmailService(mailSender, userRepository);
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:5500/frontend/html/");
        ReflectionTestUtils.setField(service, "mailUsername", "noreply@example.com");
    }

    @Test
    void sendVerificationAndPasswordEmail_setsTokenAndSendsCombinedLink() {
        User user = user("candidate@example.com", "Candidate");

        service.sendVerificationAndPasswordEmail(user);

        assertNotNull(user.getVerificationToken());
        assertNotNull(user.getTokenExpiry());
        verify(userRepository).save(user);

        SimpleMailMessage message = capturedMessage();
        assertArrayEquals(new String[]{"candidate@example.com"}, message.getTo());
        assertEquals("Complete Your Interview Tracker Registration", message.getSubject());
        assertTrue(message.getText().contains("verify-password.html?token=" + user.getVerificationToken()));
        assertEquals("noreply@example.com", message.getFrom());
    }

    @Test
    void sendPanelAssignmentEmails_sendsPanelMessagesAndCandidateMessage() {
        User candidateUser = user("candidate@example.com", "Candidate");
        Interview interview = interview();
        Panel panel1 = panel("panel1@example.com", "Panel One");
        Panel panel2 = panel("panel2@example.com", "Panel Two");

        service.sendPanelAssignmentEmails(candidateUser, List.of(panel1, panel2), interview);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(3)).send(captor.capture());
        List<SimpleMailMessage> messages = captor.getAllValues();

        assertEquals("New Interview Assigned - Interview Tracker", messages.get(0).getSubject());
        assertEquals("New Interview Assigned - Interview Tracker", messages.get(1).getSubject());
        assertEquals("Panel Assigned to Your Application - Interview Tracker", messages.get(2).getSubject());
        assertTrue(messages.get(2).getText().contains("panel1@example.com, panel2@example.com"));
        assertTrue(messages.get(2).getText().contains("Java Developer"));
    }

    @Test
    void sendCandidateSelectedEmail_withoutEmail_doesNothing() {
        User user = new User();

        service.sendCandidateSelectedEmail(user, "Java Developer");

        verifyNoInteractions(mailSender);
    }

    @Test
    void sendVerificationPasswordSetAndResetEmails_useExpectedSubjectsAndUrls() {
        User verify = user("verify@example.com", "Verify User");
        service.sendVerificationEmail(verify);
        assertEquals("Verify your Interview Tracker Account", capturedMessages(1).get(0).getSubject());

        reset(mailSender, userRepository);
        User passwordSet = user("set@example.com", "Set User");
        service.sendPasswordSetEmail(passwordSet);
        SimpleMailMessage passwordSetMessage = capturedMessages(1).get(0);
        assertEquals("Set Your Password - Interview Tracker", passwordSetMessage.getSubject());
        assertTrue(passwordSetMessage.getText().contains("verify-password.html?token=" + passwordSet.getVerificationToken()));

        reset(mailSender, userRepository);
        User reset = user("reset@example.com", "Reset User");
        service.sendPasswordResetEmail(reset);
        assertEquals("Reset Your Password - Interview Tracker", capturedMessages(1).get(0).getSubject());
    }

    @Test
    void sendPanelAssignmentEmails_validatesRequiredInputsAndUsesFallbackValues() {
        assertEquals("Candidate email not found", assertThrows(IllegalArgumentException.class,
                () -> service.sendPanelAssignmentEmails(null, List.of(panel("panel@example.com", "Panel")), new Interview())).getMessage());

        assertEquals("Candidate email not found", assertThrows(IllegalArgumentException.class,
                () -> service.sendPanelAssignmentEmails(new User(), List.of(panel("panel@example.com", "Panel")), new Interview())).getMessage());

        assertEquals("At least one panel member is required", assertThrows(IllegalArgumentException.class,
                () -> service.sendPanelAssignmentEmails(user("candidate@example.com", "Candidate"), null, new Interview())).getMessage());

        assertEquals("At least one panel member is required", assertThrows(IllegalArgumentException.class,
                () -> service.sendPanelAssignmentEmails(user("candidate@example.com", "Candidate"), List.of(), new Interview())).getMessage());

        User candidate = user("candidate@example.com", null);
        Interview interview = new Interview();
        Panel blankPanel = panel(" ", null);
        Panel panel = panel("panel@example.com", null);

        service.sendPanelAssignmentEmails(candidate, List.of(blankPanel, panel), interview);

        List<SimpleMailMessage> messages = capturedMessages(2);
        assertEquals("panel@example.com", messages.get(0).getTo()[0]);
        assertTrue(messages.get(0).getText().contains("Hello Panel Member"));
        assertTrue(messages.get(0).getText().contains("Job: N/A"));
        assertTrue(messages.get(0).getText().contains("Time: To be scheduled"));
        assertTrue(messages.get(1).getText().contains("Hello Candidate"));
        assertTrue(messages.get(1).getText().contains("panel@example.com"));
    }

    @Test
    void sendPanelAssignmentEmails_coversNullInterviewAndBlankDetailFallbacks() {
        User candidate = user("candidate@example.com", "Candidate");
        Panel nullEmail = panel(null, "No Email");
        Panel panel = panel("panel@example.com", "");

        service.sendPanelAssignmentEmails(candidate, List.of(nullEmail, panel), null);

        List<SimpleMailMessage> nullInterviewMessages = capturedMessages(2);
        assertEquals("panel@example.com", nullInterviewMessages.get(0).getTo()[0]);
        assertTrue(nullInterviewMessages.get(0).getText().contains("Job: N/A"));
        assertTrue(nullInterviewMessages.get(0).getText().contains("Round: N/A"));
        assertTrue(nullInterviewMessages.get(0).getText().contains("Focus area: General"));
        assertTrue(nullInterviewMessages.get(0).getText().contains("Duration: N/A"));
        assertTrue(nullInterviewMessages.get(0).getText().contains("Type: N/A"));
        assertTrue(nullInterviewMessages.get(0).getText().contains("Meeting link: N/A"));

        reset(mailSender);
        Interview blankDetails = new Interview();
        Candidate interviewCandidate = new Candidate();
        interviewCandidate.setJd(new JobDescription());
        blankDetails.setCandidate(interviewCandidate);
        blankDetails.setRound(null);
        blankDetails.setFocusArea(null);
        blankDetails.setDuration(null);
        blankDetails.setInterviewType(" ");
        blankDetails.setMeetingLink(" ");
        blankDetails.setNotes("These are notes");

        service.sendPanelAssignmentEmails(candidate, List.of(panel), blankDetails);

        List<SimpleMailMessage> blankDetailMessages = capturedMessages(2);
        assertTrue(blankDetailMessages.get(0).getText().contains("Notes: These are notes"));
        assertTrue(blankDetailMessages.get(1).getText().contains("Meeting link: N/A"));
    }

    @Test
    void sendCandidateSelectionAndRejectionEmails_useFallbacksAndSkipBlankEmails() {
        service.sendCandidateSelectedEmail(null, "Java Developer");

        User selected = user("selected@example.com", " ");
        service.sendCandidateSelectedEmail(selected, " ");

        User selectedFull = user("selected-full@example.com", "Selected User");
        service.sendCandidateSelectedEmail(selectedFull, "Java Developer");

        service.sendCandidateRejectedEmail(null, "Java Developer");

        User rejected = user("rejected@example.com", null);
        service.sendCandidateRejectedEmail(rejected, null);

        User rejectedFull = user("rejected-full@example.com", "Rejected User");
        service.sendCandidateRejectedEmail(rejectedFull, "Java Developer");

        User blank = user(" ", "Blank");
        service.sendCandidateRejectedEmail(blank, "Role");

        List<SimpleMailMessage> messages = capturedMessages(4);
        assertTrue(messages.get(0).getText().contains("Hello Candidate"));
        assertTrue(messages.get(0).getText().contains("the role"));
        assertTrue(messages.get(1).getText().contains("Hello Selected User"));
        assertTrue(messages.get(1).getText().contains("Java Developer"));
        assertTrue(messages.get(2).getText().contains("Hello Candidate"));
        assertTrue(messages.get(2).getText().contains("the role"));
        assertTrue(messages.get(3).getText().contains("Hello Rejected User"));
        assertTrue(messages.get(3).getText().contains("Java Developer"));
    }

    @Test
    void sendPasswordResetEmail_whenMailSenderFails_wrapsException() {
        User user = user("candidate@example.com", "Candidate");
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.sendPasswordResetEmail(user));

        assertEquals("Email sending failed", ex.getMessage());
        verify(userRepository).save(user);
    }

    @Test
    void registrationEmailMethods_wrapMailSenderFailures() {
        User user = user("candidate@example.com", "Candidate");
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertEquals("Email sending failed", assertThrows(RuntimeException.class,
                () -> service.sendVerificationEmail(user)).getMessage());

        reset(mailSender, userRepository);
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));
        assertEquals("Email sending failed", assertThrows(RuntimeException.class,
                () -> service.sendVerificationAndPasswordEmail(user)).getMessage());

        reset(mailSender, userRepository);
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(any(SimpleMailMessage.class));
        assertEquals("Email sending failed", assertThrows(RuntimeException.class,
                () -> service.sendPasswordSetEmail(user)).getMessage());
    }

    private SimpleMailMessage capturedMessage() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    private List<SimpleMailMessage> capturedMessages(int count) {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(count)).send(captor.capture());
        return captor.getAllValues();
    }

    private User user(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private Panel panel(String email, String name) {
        Panel panel = new Panel();
        panel.setEmail(email);
        panel.setName(name);
        return panel;
    }

    private Interview interview() {
        JobDescription jd = new JobDescription();
        jd.setTitle("Java Developer");
        Candidate candidate = new Candidate();
        candidate.setJd(jd);
        Interview interview = new Interview();
        interview.setCandidate(candidate);
        interview.setRound("L1");
        interview.setFocusArea("Java");
        interview.setInterviewTime(LocalDateTime.of(2026, 5, 6, 10, 0));
        interview.setDuration(45);
        interview.setInterviewType("Online");
        interview.setMeetingLink("https://meet.example.com/java");
        return interview;
    }
}

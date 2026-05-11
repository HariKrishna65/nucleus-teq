package com.interview.tracker.service;

import com.interview.tracker.constants.Stage;
import com.interview.tracker.constants.StageStatus;
import com.interview.tracker.entity.Candidate;
import com.interview.tracker.entity.JobDescription;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.CandidateRepository;
import com.interview.tracker.repository.JobDescriptionRepository;
import com.interview.tracker.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JobDescriptionRepository jobDescriptionRepository;

    private CandidateService service;

    @BeforeEach
    void setUp() {
        service = new CandidateService(candidateRepository, userRepository, jobDescriptionRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createCandidate_withPdfResume_setsDefaultsAndSaves() throws Exception {
        User userRef = new User();
        userRef.setId(1L);
        JobDescription jdRef = new JobDescription();
        jdRef.setId(2L);
        Candidate candidate = new Candidate();
        candidate.setUser(userRef);
        candidate.setJd(jdRef);
        candidate.setPhone("9876543210");

        User savedUser = new User();
        savedUser.setId(1L);
        JobDescription savedJd = new JobDescription();
        savedJd.setId(2L);
        MockMultipartFile resume = new MockMultipartFile("file", "resume.pdf", "application/pdf", "pdf".getBytes());

        when(candidateRepository.findByUser_Id(1L)).thenReturn(List.of());
        when(candidateRepository.findByUser_IdAndJd_Id(1L, 2L)).thenReturn(Optional.empty());
        when(candidateRepository.findByPhone("9876543210")).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(savedUser));
        when(jobDescriptionRepository.findById(2L)).thenReturn(Optional.of(savedJd));
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Candidate saved = service.createCandidate(candidate, resume);

        assertEquals(savedUser, saved.getUser());
        assertEquals(savedJd, saved.getJd());
        assertEquals("APPLIED", saved.getStatus());
        assertEquals(Stage.PROFILING, saved.getStage());
        assertEquals(StageStatus.COMPLETED, saved.getStageStatus());
        assertEquals("resume.pdf", saved.getResumeFileName());
        assertArrayEquals("pdf".getBytes(), saved.getResumeData());
        assertNotNull(saved.getApplicationDate());
    }

    @Test
    void createCandidate_withActiveUserApplication_rejects() {
        Candidate candidate = candidateWithUserAndJd(1L, 2L);
        when(candidateRepository.findByUser_Id(1L)).thenReturn(List.of(new Candidate()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, null));

        assertEquals("Only one active job application is allowed per candidate", ex.getMessage());
        verify(candidateRepository, never()).save(any());
    }

    @Test
    void createCandidate_withOnlyRejectedPreviousApplication_allowsDifferentJob() throws Exception {
        Candidate candidate = candidateWithUserAndJd(1L, 3L);
        candidate.setPhone("9876543210");
        User existingUser = new User();
        existingUser.setId(1L);
        JobDescription newJd = new JobDescription();
        newJd.setId(3L);
        Candidate rejected = candidateWithUserAndJd(1L, 2L);
        rejected.setStage(Stage.REJECTED);

        when(candidateRepository.findByUser_Id(1L)).thenReturn(List.of(rejected));
        when(candidateRepository.findByUser_IdAndJd_Id(1L, 3L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(jobDescriptionRepository.findById(3L)).thenReturn(Optional.of(newJd));
        when(candidateRepository.findByPhone("9876543210")).thenReturn(Optional.of(rejected));
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Candidate saved = service.createCandidate(candidate, null);

        assertEquals(existingUser, saved.getUser());
        assertEquals(newJd, saved.getJd());
        assertEquals("APPLIED", saved.getStatus());
        assertEquals(Stage.PROFILING, saved.getStage());
    }

    @Test
    void createCandidate_withNullPayload_rejects() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(null, null));

        assertEquals("Candidate payload is required", ex.getMessage());
        verifyNoInteractions(candidateRepository, userRepository, jobDescriptionRepository);
    }

    @Test
    void createCandidate_withoutUser_rejects() {
        Candidate candidate = new Candidate();
        candidate.setJd(new JobDescription());
        candidate.getJd().setId(2L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, null));

        assertEquals("User is required", ex.getMessage());
    }

    @Test
    void createCandidate_withUserMissingId_rejects() {
        Candidate candidate = new Candidate();
        candidate.setUser(new User());
        JobDescription jd = new JobDescription();
        jd.setId(2L);
        candidate.setJd(jd);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, null));

        assertEquals("User is required", ex.getMessage());
    }

    @Test
    void createCandidate_withoutJobDescription_rejects() {
        Candidate candidate = new Candidate();
        User user = new User();
        user.setId(1L);
        candidate.setUser(user);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, null));

        assertEquals("Job description is required", ex.getMessage());
    }

    @Test
    void createCandidate_withJobDescriptionMissingId_rejects() {
        Candidate candidate = new Candidate();
        User user = new User();
        user.setId(1L);
        candidate.setUser(user);
        candidate.setJd(new JobDescription());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, null));

        assertEquals("Job description is required", ex.getMessage());
    }

    @Test
    void createCandidate_asCandidateForAnotherUser_rejects() {
        setJwt("candidate@example.com", 5L, "CANDIDATE");
        Candidate candidate = candidateWithUserAndJd(1L, 2L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, null));

        assertEquals("Candidates can only apply for themselves", ex.getMessage());
        verifyNoInteractions(candidateRepository);
    }

    @Test
    void createCandidate_asCandidateWithoutUserIdClaim_rejects() {
        setJwt("candidate@example.com", null, "CANDIDATE");
        Candidate candidate = candidateWithUserAndJd(1L, 2L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, null));

        assertEquals("Candidates can only apply for themselves", ex.getMessage());
    }

    @Test
    void createCandidate_whenSameUserAlreadyAppliedForJob_rejects() {
        Candidate candidate = candidateWithUserAndJd(1L, 2L);
        when(candidateRepository.findByUser_Id(1L)).thenReturn(List.of());
        when(candidateRepository.findByUser_IdAndJd_Id(1L, 2L)).thenReturn(Optional.of(new Candidate()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, null));

        assertEquals("You have already applied for this job", ex.getMessage());
    }

    @Test
    void createCandidate_whenUserMissing_rejects() {
        Candidate candidate = candidateWithUserAndJd(1L, 2L);
        when(candidateRepository.findByUser_Id(1L)).thenReturn(List.of());
        when(candidateRepository.findByUser_IdAndJd_Id(1L, 2L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, null));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void createCandidate_whenJobDescriptionMissing_rejects() {
        Candidate candidate = candidateWithUserAndJd(1L, 2L);
        when(candidateRepository.findByUser_Id(1L)).thenReturn(List.of());
        when(candidateRepository.findByUser_IdAndJd_Id(1L, 2L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(jobDescriptionRepository.findById(2L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, null));

        assertEquals("Job description not found", ex.getMessage());
    }

    @Test
    void createCandidate_withDuplicatePhone_rejects() {
        Candidate candidate = candidateWithUserAndJd(1L, 2L);
        candidate.setPhone("9876543210");
        when(candidateRepository.findByUser_Id(1L)).thenReturn(List.of());
        when(candidateRepository.findByUser_IdAndJd_Id(1L, 2L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(jobDescriptionRepository.findById(2L)).thenReturn(Optional.of(new JobDescription()));
        when(candidateRepository.findByPhone("9876543210")).thenReturn(Optional.of(new Candidate()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, null));

        assertEquals("Mobile number already exists", ex.getMessage());
    }

    @Test
    void createCandidate_withExistingStatusAndStage_keepsProvidedValues() throws Exception {
        Candidate candidate = candidateWithUserAndJd(1L, 2L);
        candidate.setStatus("REFERRED");
        candidate.setStage(Stage.SCREENING);
        candidate.setStageStatus(StageStatus.PENDING);
        when(candidateRepository.findByUser_Id(1L)).thenReturn(List.of());
        when(candidateRepository.findByUser_IdAndJd_Id(1L, 2L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(jobDescriptionRepository.findById(2L)).thenReturn(Optional.of(new JobDescription()));
        when(candidateRepository.save(candidate)).thenReturn(candidate);

        Candidate saved = service.createCandidate(candidate, null);

        assertEquals("REFERRED", saved.getStatus());
        assertEquals(Stage.SCREENING, saved.getStage());
        assertEquals(StageStatus.PENDING, saved.getStageStatus());
    }

    @Test
    void createCandidate_withBlankStatus_setsApplied() throws Exception {
        Candidate candidate = candidateWithUserAndJd(1L, 2L);
        candidate.setStatus(" ");
        when(candidateRepository.findByUser_Id(1L)).thenReturn(List.of());
        when(candidateRepository.findByUser_IdAndJd_Id(1L, 2L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(jobDescriptionRepository.findById(2L)).thenReturn(Optional.of(new JobDescription()));
        when(candidateRepository.save(candidate)).thenReturn(candidate);

        Candidate saved = service.createCandidate(candidate, null);

        assertEquals("APPLIED", saved.getStatus());
    }

    @Test
    void createCandidate_withEmptyResumeFile_ignoresResume() throws Exception {
        Candidate candidate = candidateWithUserAndJd(1L, 2L);
        MockMultipartFile emptyResume = new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[0]);
        when(candidateRepository.findByUser_Id(1L)).thenReturn(List.of());
        when(candidateRepository.findByUser_IdAndJd_Id(1L, 2L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(jobDescriptionRepository.findById(2L)).thenReturn(Optional.of(new JobDescription()));
        when(candidateRepository.save(candidate)).thenReturn(candidate);

        Candidate saved = service.createCandidate(candidate, emptyResume);

        assertNull(saved.getResumeFileName());
        assertNull(saved.getResumeData());
    }

    @Test
    void createCandidate_withResumeMissingOriginalFilename_rejects() {
        Candidate candidate = candidateWithUserAndJd(1L, 2L);
        MockMultipartFile resume = new MockMultipartFile("file", null, "application/pdf", "pdf".getBytes());
        when(candidateRepository.findByUser_Id(1L)).thenReturn(List.of());
        when(candidateRepository.findByUser_IdAndJd_Id(1L, 2L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(jobDescriptionRepository.findById(2L)).thenReturn(Optional.of(new JobDescription()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, resume));

        assertEquals("Resume must be a PDF file", ex.getMessage());
    }

    @Test
    void createCandidate_withNonPdfResume_rejects() {
        Candidate candidate = candidateWithUserAndJd(1L, 2L);
        when(candidateRepository.findByUser_Id(1L)).thenReturn(List.of());
        when(candidateRepository.findByUser_IdAndJd_Id(1L, 2L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(jobDescriptionRepository.findById(2L)).thenReturn(Optional.of(new JobDescription()));

        MockMultipartFile resume = new MockMultipartFile("file", "resume.txt", "text/plain", "text".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createCandidate(candidate, resume));

        assertEquals("Resume must be a PDF file", ex.getMessage());
        verify(candidateRepository, never()).save(any());
    }

    @Test
    void getByUserScoped_asCandidateForAnotherUser_rejects() {
        setJwt("candidate@example.com", 5L, "CANDIDATE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getByUserScoped(9L));

        assertEquals("You can only view your own applications", ex.getMessage());
    }

    @Test
    void getByUserScoped_asCandidateForSelf_returnsApplications() {
        setJwt("candidate@example.com", 5L, "CANDIDATE");
        Candidate candidate = new Candidate();
        when(candidateRepository.findByUser_Id(5L)).thenReturn(List.of(candidate));

        assertEquals(List.of(candidate), service.getByUserScoped(5L));
    }

    @Test
    void getByUser_delegatesToRepository() {
        Candidate candidate = new Candidate();
        when(candidateRepository.findByUser_Id(3L)).thenReturn(List.of(candidate));

        assertEquals(List.of(candidate), service.getByUser(3L));
    }

    @Test
    void getCandidateById_whenMissing_rejects() {
        when(candidateRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getCandidateById(99L));

        assertEquals("Candidate not found", ex.getMessage());
    }

    private Candidate candidateWithUserAndJd(Long userId, Long jdId) {
        User user = new User();
        user.setId(userId);
        JobDescription jd = new JobDescription();
        jd.setId(jdId);
        Candidate candidate = new Candidate();
        candidate.setUser(user);
        candidate.setJd(jd);
        return candidate;
    }

    private void setJwt(String subject, Long userId, String role) {
        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("sub", subject);
        claims.put("role", role);
        if (userId != null) {
            claims.put("userId", userId);
        }
        Jwt jwt = new Jwt("token", null, null, Map.of("alg", "none"), claims);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}

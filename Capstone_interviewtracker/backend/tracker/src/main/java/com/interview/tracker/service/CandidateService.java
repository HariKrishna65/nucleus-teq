package com.interview.tracker.service;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.entity.JobDescription;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.CandidateRepository;
import com.interview.tracker.repository.JobDescriptionRepository;
import com.interview.tracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.interview.tracker.constants.Stage;
import com.interview.tracker.constants.StageStatus;

@Service
public class CandidateService {
    private static final Logger log = LoggerFactory.getLogger(CandidateService.class);

    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final JobDescriptionRepository jobDescriptionRepository;

    public CandidateService(
            CandidateRepository candidateRepository,
            UserRepository userRepository,
            JobDescriptionRepository jobDescriptionRepository
    ) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
    }

    public Candidate createCandidate(Candidate candidate, MultipartFile file) throws IOException {

        if (candidate == null) {
            throw new IllegalArgumentException("Candidate payload is required");
        }

        if (candidate.getUser() == null || candidate.getUser().getId() == null) {
            throw new IllegalArgumentException("User is required");
        }

        if (candidate.getJd() == null || candidate.getJd().getId() == null) {
            throw new IllegalArgumentException("Job description is required");
        }

        Long userId = candidate.getUser().getId();
        Long jdId = candidate.getJd().getId();

        // Access control
        String role = SecurityUtil.currentRole();
        Long currentUserId = SecurityUtil.currentUserId();
        if (com.interview.tracker.constants.AppConstants.ROLE_CANDIDATE.equals(role)
                && (currentUserId == null || !currentUserId.equals(userId))) {
            throw new IllegalArgumentException("Candidates can only apply for themselves");
        }

        if (!candidateRepository.findByUser_Id(userId).isEmpty()) {
            throw new IllegalArgumentException("Only one job application is allowed per candidate");
        }

        if (candidateRepository.findByUser_IdAndJd_Id(userId, jdId).isPresent()) {
            throw new IllegalArgumentException("You have already applied for this job");
        }

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        candidate.setUser(existingUser);

        JobDescription existingJd = jobDescriptionRepository.findById(jdId)
                .orElseThrow(() -> new IllegalArgumentException("Job description not found"));
        candidate.setJd(existingJd);

        if (candidate.getPhone() != null && !candidate.getPhone().isBlank()) {
            candidateRepository.findByPhone(candidate.getPhone())
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Mobile number already exists");
                    });
        }

        if (candidate.getStatus() == null || candidate.getStatus().isBlank()) {
            candidate.setStatus("APPLIED");
        }
        candidate.setApplicationDate(LocalDateTime.now());
        if (candidate.getStage() == null) {
            candidate.setStage(Stage.PROFILING);
            candidate.setStageStatus(StageStatus.COMPLETED);
        }

        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename() == null ? "" 
                                  : file.getOriginalFilename().toLowerCase();
            if (!originalName.endsWith(".pdf")) {
                throw new IllegalArgumentException("Resume must be a PDF file");
            }

            candidate.setResumeData(file.getBytes());
            candidate.setResumeFileName(file.getOriginalFilename());
        }

        Candidate saved = candidateRepository.save(candidate);
        log.info("Candidate application submitted: candidateId={}, userId={}, jdId={}", 
                 saved.getId(), userId, jdId);
        return saved;
    }

    public List<Candidate> getByUser(Long userId) {
        return candidateRepository.findByUser_Id(userId);
    }

    public List<Candidate> getByUserScoped(Long requestedUserId) {
        String role = SecurityUtil.currentRole();
        Long currentUserId = SecurityUtil.currentUserId();

        if (com.interview.tracker.constants.AppConstants.ROLE_CANDIDATE.equals(role)
                && (currentUserId == null || requestedUserId == null 
                    || !requestedUserId.equals(currentUserId))) {
            throw new IllegalArgumentException("You can only view your own applications");
        }
        return candidateRepository.findByUser_Id(requestedUserId);
    }

    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));
    }
}
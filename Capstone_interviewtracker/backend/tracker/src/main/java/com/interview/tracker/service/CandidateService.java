package com.interview.tracker.service;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.CandidateRepository;
import com.interview.tracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.interview.tracker.constants.Stage;
import com.interview.tracker.constants.StageStatus;

@Service
public class CandidateService {
    private static final Logger log = LoggerFactory.getLogger(CandidateService.class);

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR = "uploads/";

    public Candidate createCandidate(Candidate candidate, MultipartFile file) throws IOException {

        if (candidate.getUser() == null || candidate.getUser().getId() == null) {
            throw new IllegalArgumentException("User is required");
        }

        if (candidate.getJd() == null || candidate.getJd().getId() == null) {
            throw new IllegalArgumentException("Job description is required");
        }

        Long userId = candidate.getUser().getId();
        Long jdId = candidate.getJd().getId();

        if (!candidateRepository.findByUser_Id(userId).isEmpty()) {
            throw new IllegalArgumentException("Only one job application is allowed per candidate");
        }

        if (candidateRepository.findByUser_IdAndJd_Id(userId, jdId).isPresent()) {
            throw new IllegalArgumentException("You have already applied for this job");
        }

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        candidate.setUser(existingUser);

        if (candidate.getPhone() != null && !candidate.getPhone().isBlank()) {
            candidateRepository.findByPhone(candidate.getPhone())
                    .ifPresent(existing -> { throw new IllegalArgumentException("Mobile number already exists"); });
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
            String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
            if (!originalName.endsWith(".pdf")) {
                throw new IllegalArgumentException("Resume must be a PDF file");
            }

            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String filePath = UPLOAD_DIR + fileName;

            file.transferTo(new File(filePath));

            candidate.setResumeUrl(filePath);
        }
        Candidate saved = candidateRepository.save(candidate);
        log.info("Candidate application submitted: candidateId={}, userId={}, jdId={}", saved.getId(), userId, jdId);
        return saved;
    }

    public List<Candidate> getByUser(Long userId) {
        return candidateRepository.findByUser_Id(userId);
    }
}
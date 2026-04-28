package com.interview.tracker.controller;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.service.CandidateService;
import com.interview.tracker.service.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.interview.tracker.constants.AppConstants.CANDIDATES;

/**
 * Controller for candidate operations such as applying and dashboard.
 */
@RestController
@RequestMapping(CANDIDATES)
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    /**
     * Apply for job (create candidate + upload resume).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Candidate createCandidate(
            @Valid @RequestPart("candidate") Candidate candidate,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws Exception {
        String role = SecurityUtil.currentRole();
        Long currentUserId = SecurityUtil.currentUserId();
        Long requestedUserId = candidate != null && candidate.getUser() != null ? candidate.getUser().getId() : null;

        if ("CANDIDATE".equals(role) && (requestedUserId == null || !requestedUserId.equals(currentUserId))) {
            throw new IllegalArgumentException("Candidates can only apply for themselves");
        }

        return candidateService.createCandidate(candidate, file);
    }

    /**
     * Fetch candidates for dashboard (status tracking).
     */
    @GetMapping
    public ResponseEntity<?> getByUser(@RequestParam Long userId) {
        String role = SecurityUtil.currentRole();
        Long currentUserId = SecurityUtil.currentUserId();

        if ("CANDIDATE".equals(role) && !userId.equals(currentUserId)) {
            return ResponseEntity.status(403).body("You can only view your own applications");
        }
        return ResponseEntity.ok(candidateService.getByUser(userId));
    }
}
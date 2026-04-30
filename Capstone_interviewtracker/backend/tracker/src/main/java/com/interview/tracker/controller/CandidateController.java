package com.interview.tracker.controller;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.service.CandidateService;
import com.interview.tracker.service.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.interview.tracker.constants.AppConstants.CANDIDATES;
import static com.interview.tracker.constants.AppConstants.ROLE_CANDIDATE;

@RestController
@RequestMapping(CANDIDATES)
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Candidate createCandidate(
            @Valid @RequestPart("candidate") Candidate candidate,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws Exception {
        return candidateService.createCandidate(candidate, file);
    }

    @GetMapping
    public ResponseEntity<?> getByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(candidateService.getByUserScoped(userId));
    }
}
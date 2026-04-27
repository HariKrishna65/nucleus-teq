package com.interview.tracker.controller;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
            @RequestPart("candidate") Candidate candidate,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws Exception {
        return candidateService.createCandidate(candidate, file);
    }

    /**
     * Fetch candidates for dashboard (status tracking).
     */
    @GetMapping
    public List<Candidate> getByUser(@RequestParam Long userId) {
        return candidateService.getByUser(userId);
    }
}
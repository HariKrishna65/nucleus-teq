package com.interview.tracker.controller;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/candidates")
@CrossOrigin("*")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    // ✅ Apply (create candidate + upload resume)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Candidate createCandidate(
            @RequestPart("candidate") Candidate candidate,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws Exception {

        return candidateService.createCandidate(candidate, file);
    }

    // ✅ Dashboard (status tracking)
    @GetMapping
    public List<Candidate> getByUser(@RequestParam Long userId) {
        return candidateService.getByUser(userId);
    }
}
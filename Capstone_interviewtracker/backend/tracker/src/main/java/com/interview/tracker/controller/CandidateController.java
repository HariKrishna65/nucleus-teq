package com.interview.tracker.controller;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/candidates")
@CrossOrigin("*")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    // ✅ Create candidate with resume upload
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Candidate createCandidate(
            @RequestPart("candidate") Candidate candidate,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws Exception {

        return candidateService.createCandidate(candidate, file);
    }
}
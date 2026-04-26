package com.interview.tracker.controller;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidates")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    // ✅ Create Candidate
    @PostMapping
    public Candidate createCandidate(@RequestBody Candidate candidate) {
        return candidateService.saveCandidate(candidate);
    }

    // ✅ Get all
    @GetMapping
    public List<Candidate> getAllCandidates() {
        return candidateService.getAllCandidates();
    }

    // ✅ Get by ID
    @GetMapping("/{id}")
    public Candidate getById(@PathVariable Long id) {
        return candidateService.getCandidateById(id);
    }

    // 🔥 Search by email (via User)
    @GetMapping("/search")
    public List<Candidate> search(@RequestParam String email) {
        return candidateService.searchByEmail(email);
    }
    

    // ✅ Delete
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        candidateService.deleteCandidate(id);
        return "Candidate deleted successfully";
    }
}
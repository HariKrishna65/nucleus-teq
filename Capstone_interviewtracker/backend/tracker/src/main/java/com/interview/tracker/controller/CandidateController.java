package com.interview.tracker.controller;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "*")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @GetMapping
    public List<Candidate> getAllCandidates() {
        return candidateService.getAllCandidates();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Candidate> getCandidateById(@PathVariable Long id) {
        return candidateService.getCandidateById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Candidate createCandidate(@RequestBody Candidate candidate) {
        return candidateService.saveCandidate(candidate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Candidate> updateCandidate(@PathVariable Long id, @RequestBody Candidate candidate) {
        return candidateService.getCandidateById(id)
            .map(existing -> {
                candidate.setId(id);
                return ResponseEntity.ok(candidateService.saveCandidate(candidate));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        if (candidateService.getCandidateById(id).isPresent()) {
            candidateService.deleteCandidate(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/status/{status}")
    public List<Candidate> getCandidatesByStatus(@PathVariable Candidate.Status status) {
        return candidateService.getCandidatesByStatus(status);
    }

    @GetMapping("/search")
    public List<Candidate> searchCandidates(@RequestParam String email) {
        return candidateService.searchCandidates(email);
    }
}
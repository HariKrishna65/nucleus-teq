package com.interview.tracker.service;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    // ✅ Create
    public Candidate saveCandidate(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    // ✅ Get all
    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    // ✅ Get by ID
    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id).orElse(null);
    }

    // 🔥 FIXED METHOD (your previous error was here)
    public List<Candidate> searchByEmail(String email) {
        return candidateRepository.findByUser_EmailContaining(email);
    }

    // ✅ Delete
    public void deleteCandidate(Long id) {
        candidateRepository.deleteById(id);
    }
}
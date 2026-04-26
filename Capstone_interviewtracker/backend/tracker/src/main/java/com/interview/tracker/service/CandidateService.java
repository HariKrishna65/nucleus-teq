package com.interview.tracker.service;

import com.interview.tracker.entity.Candidate;
import com.interview.tracker.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    public Optional<Candidate> getCandidateById(Long id) {
        return candidateRepository.findById(id);
    }

    public Candidate saveCandidate(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    public void deleteCandidate(Long id) {
        candidateRepository.deleteById(id);
    }

    public List<Candidate> getCandidatesByStatus(Candidate.Status status) {
        return candidateRepository.findByStatus(status);
    }

    public List<Candidate> searchCandidates(String email) {
        return candidateRepository.findByEmailContaining(email);
    }
}
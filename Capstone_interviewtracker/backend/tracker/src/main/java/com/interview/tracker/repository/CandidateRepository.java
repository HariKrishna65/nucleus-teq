package com.interview.tracker.repository;

import com.interview.tracker.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    // 🔥 Correct search using nested field (User.email)
    List<Candidate> findByUser_EmailContaining(String email);
}
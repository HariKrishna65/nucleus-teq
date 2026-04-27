package com.interview.tracker.repository;

import com.interview.tracker.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    // 🔹 Get candidates by user (for dashboard)
    List<Candidate> findByUser_Id(Long userId);
}
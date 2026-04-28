package com.interview.tracker.repository;

import com.interview.tracker.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByInterview_Candidate_IdOrderByIdDesc(Long candidateId);
    Optional<Feedback> findTopByInterview_IdOrderByIdDesc(Long interviewId);
}
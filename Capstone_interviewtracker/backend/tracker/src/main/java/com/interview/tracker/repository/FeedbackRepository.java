package com.interview.tracker.repository;

import com.interview.tracker.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for feedback.
 */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
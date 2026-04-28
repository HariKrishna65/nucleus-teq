package com.interview.tracker.service;

import com.interview.tracker.entity.Feedback;
import com.interview.tracker.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for feedback operations.
 */
@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    /**
     * Save feedback.
     */
    public Feedback save(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }
}
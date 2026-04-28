package com.interview.tracker.controller;

import com.interview.tracker.entity.Feedback;
import com.interview.tracker.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for feedback submission.
 */
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * Submit feedback for an interview.
     */
    @PostMapping
    public Feedback submit(@RequestBody Feedback feedback) {
        return feedbackService.save(feedback);
    }
}
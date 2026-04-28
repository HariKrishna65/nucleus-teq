package com.interview.tracker.controller;

import com.interview.tracker.entity.Feedback;
import com.interview.tracker.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    public Feedback submit(@Valid @RequestBody Feedback feedback) {
        return feedbackService.save(feedback);
    }

    @GetMapping("/interview/{interviewId}")
    public Feedback getLatestByInterview(@PathVariable Long interviewId) {
        return feedbackService.getLatestByInterview(interviewId);
    }
}
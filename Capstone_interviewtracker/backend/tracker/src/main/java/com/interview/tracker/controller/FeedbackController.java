package com.interview.tracker.controller;

import com.interview.tracker.entity.Feedback;
import com.interview.tracker.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static com.interview.tracker.constants.AppConstants.FEEDBACK;

@RestController
@RequestMapping(FEEDBACK)
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<Feedback> submit(@Valid @RequestBody Feedback feedback) {
        return ResponseEntity.ok(feedbackService.save(feedback));
    }

    @GetMapping("/interview/{interviewId}")
    public ResponseEntity<Feedback> getLatestByInterview(@PathVariable Long interviewId) {
        return ResponseEntity.ok(feedbackService.getLatestByInterview(interviewId));
    }
}
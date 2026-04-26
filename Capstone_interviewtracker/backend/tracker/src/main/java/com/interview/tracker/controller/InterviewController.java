package com.interview.tracker.controller;

import com.interview.tracker.entity.Interview;
import com.interview.tracker.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@CrossOrigin(origins = "*")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @GetMapping
    public List<Interview> getAllInterviews() {
        return interviewService.getAllInterviews();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Interview> getInterviewById(@PathVariable Long id) {
        return interviewService.getInterviewById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Interview createInterview(@RequestBody Interview interview) {
        return interviewService.saveInterview(interview);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Interview> updateInterview(@PathVariable Long id, @RequestBody Interview interview) {
        return interviewService.getInterviewById(id)
            .map(existing -> {
                interview.setId(id);
                return ResponseEntity.ok(interviewService.saveInterview(interview));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long id) {
        if (interviewService.getInterviewById(id).isPresent()) {
            interviewService.deleteInterview(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/candidate/{candidateId}")
    public List<Interview> getInterviewsByCandidate(@PathVariable Long candidateId) {
        return interviewService.getInterviewsByCandidate(candidateId);
    }

    @GetMapping("/status/{status}")
    public List<Interview> getInterviewsByStatus(@PathVariable Interview.Status status) {
        return interviewService.getInterviewsByStatus(status);
    }

    @GetMapping("/round/{round}")
    public List<Interview> getInterviewsByRound(@PathVariable Interview.Round round) {
        return interviewService.getInterviewsByRound(round);
    }
}
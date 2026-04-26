package com.interview.tracker.controller;

import com.interview.tracker.entity.Interview;
import com.interview.tracker.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interviews")
public class InterviewController {

    @Autowired
    private InterviewService service;

    @PostMapping
    public Interview createInterview(@RequestBody Interview interview) {
        return service.saveInterview(interview);
    }

    @GetMapping
    public List<Interview> getAll() {
        return service.getAllInterviews();
    }

    @GetMapping("/{id}")
    public Interview getById(@PathVariable Long id) {
        return service.getInterviewById(id);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteInterview(id);
        return "Deleted successfully";
    }
}
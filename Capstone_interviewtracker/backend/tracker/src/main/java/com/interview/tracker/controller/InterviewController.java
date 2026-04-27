package com.interview.tracker.controller;

import com.interview.tracker.entity.Interview;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.repository.PanelRepository;
import com.interview.tracker.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interviews")
@CrossOrigin("*")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private PanelRepository panelRepository;

    //Create Panel
    @PostMapping("/panel")
    public Panel createPanel(@RequestBody Panel panel) {
        return panelRepository.save(panel);
    }

    // Get Panels
    @GetMapping("/panel")
    public List<Panel> getPanels() {
        return panelRepository.findAll();
    }

    //Schedule Interview
    @PostMapping
    public Interview scheduleInterview(@RequestBody Interview interview) {
        return interviewService.scheduleInterview(interview);
    }
}
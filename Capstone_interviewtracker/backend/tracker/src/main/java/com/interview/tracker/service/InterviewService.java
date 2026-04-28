package com.interview.tracker.service;

import com.interview.tracker.entity.Interview;
import com.interview.tracker.repository.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for interview operations.
 */
@Service
public class InterviewService {

    @Autowired
    private InterviewRepository interviewRepository;

    /**
     * Schedule a new interview.
     */
    public Interview scheduleInterview(Interview interview) {
        return interviewRepository.save(interview);
    }

    /**
     * Get interviews assigned to a specific panel.
     */
    public List<Interview> getByPanel(Long panelId) {
        return interviewRepository.findByPanel_Id(panelId);
    }
}
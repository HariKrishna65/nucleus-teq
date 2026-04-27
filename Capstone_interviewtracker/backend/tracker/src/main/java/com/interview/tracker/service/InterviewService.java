package com.interview.tracker.service;

import com.interview.tracker.entity.Interview;
import com.interview.tracker.repository.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InterviewService {

    @Autowired
    private InterviewRepository interviewRepository;

    public Interview scheduleInterview(Interview interview) {
        return interviewRepository.save(interview);
    }
}
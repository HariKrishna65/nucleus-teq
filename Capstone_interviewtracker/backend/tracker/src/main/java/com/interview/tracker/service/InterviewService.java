package com.interview.tracker.service;

import com.interview.tracker.entity.Interview;
import com.interview.tracker.repository.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterviewService {

    @Autowired
    private InterviewRepository repository;

    public Interview saveInterview(Interview interview) {
        return repository.save(interview);
    }

    public List<Interview> getAllInterviews() {
        return repository.findAll();
    }

    public Interview getInterviewById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deleteInterview(Long id) {
        repository.deleteById(id);
    }
}
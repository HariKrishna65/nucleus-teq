package com.interview.tracker.service;

import com.interview.tracker.entity.Interview;
import com.interview.tracker.repository.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class InterviewService {

    @Autowired
    private InterviewRepository interviewRepository;

    public List<Interview> getAllInterviews() {
        return interviewRepository.findAll();
    }

    public Optional<Interview> getInterviewById(Long id) {
        return interviewRepository.findById(id);
    }

    public Interview saveInterview(Interview interview) {
        return interviewRepository.save(interview);
    }

    public void deleteInterview(Long id) {
        interviewRepository.deleteById(id);
    }

    public List<Interview> getInterviewsByCandidate(Long candidateId) {
        return interviewRepository.findByCandidateId(candidateId);
    }

    public List<Interview> getInterviewsByStatus(Interview.Status status) {
        return interviewRepository.findByStatus(status);
    }

    public List<Interview> getInterviewsByRound(Interview.Round round) {
        return interviewRepository.findByRound(round);
    }
}
package com.interview.tracker.service;

import com.interview.tracker.entity.JobDescription;
import com.interview.tracker.repository.JobDescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobDescriptionService {

    @Autowired
    private JobDescriptionRepository repo;

    public JobDescription save(JobDescription jd) {
        return repo.save(jd);
    }

    public List<JobDescription> getAll() {
        return repo.findAll();
    }

    public JobDescription getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
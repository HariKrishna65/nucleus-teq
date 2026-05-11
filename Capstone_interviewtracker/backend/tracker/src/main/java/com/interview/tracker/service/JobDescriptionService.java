package com.interview.tracker.service;

import com.interview.tracker.entity.JobDescription;
import com.interview.tracker.repository.JobDescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobDescriptionService {

    private final JobDescriptionRepository repo;

    public JobDescriptionService(JobDescriptionRepository repo) {
        this.repo = repo;
    }

    public JobDescription save(JobDescription jd) {
        if (jd == null) {
            throw new IllegalArgumentException("Job is required");
        }

        validateDescriptionWords(jd.getDescription());
        validateRange("Experience", jd.getExperienceMin(), jd.getExperienceMax());
        validateRange("Salary", jd.getSalaryMin(), jd.getSalaryMax());

        if (jd.getExperience() == null && jd.getExperienceMin() != null) {
            jd.setExperience(jd.getExperienceMin());
        }
        if ((jd.getSalary() == null || jd.getSalary().isBlank()) && jd.getSalaryMin() != null && jd.getSalaryMax() != null) {
            jd.setSalary(jd.getSalaryMin() + " - " + jd.getSalaryMax());
        }
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

    private void validateDescriptionWords(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        int words = (int) java.util.Arrays.stream(description.trim().split("\\s+"))
                .filter(w -> !w.isBlank())
                .count();
        if (words < 10 || words > 50) {
            throw new IllegalArgumentException("Job description must be between 10 and 50 words");
        }
    }

    private void validateRange(String label, Integer min, Integer max) {
        if (min == null || max == null) {
            throw new IllegalArgumentException(label + " min and max are required");
        }
        if (min < 0 || max < 0) {
            throw new IllegalArgumentException(label + " cannot be negative");
        }
        if (min > max) {
            throw new IllegalArgumentException(label + " min cannot be greater than max");
        }
    }
}

package com.interview.tracker.service;

import com.interview.tracker.entity.JobDescription;
import com.interview.tracker.repository.JobDescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobDescriptionServiceTest {

    private JobDescriptionRepository repo;
    private JobDescriptionService service;

    @BeforeEach
    void setUp() {
        repo = mock(JobDescriptionRepository.class);
        service = new JobDescriptionService(repo);
    }

    @Test
    void save_withValidJob_setsDerivedExperienceAndSalary() {
        JobDescription jd = validJob();
        when(repo.save(jd)).thenReturn(jd);

        JobDescription saved = service.save(jd);

        assertEquals(2, saved.getExperience());
        assertEquals("700000 - 1200000", saved.getSalary());
        verify(repo).save(jd);
    }

    @Test
    void save_withTooShortDescription_rejects() {
        JobDescription jd = validJob();
        jd.setDescription("too short");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(jd));

        assertEquals("Job description must be between 10 and 50 words", ex.getMessage());
        verify(repo, never()).save(any());
    }

    @Test
    void save_withNullJob_rejects() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(null));

        assertEquals("Job is required", ex.getMessage());
        verify(repo, never()).save(any());
    }

    @Test
    void save_withBlankDescription_rejects() {
        JobDescription jd = validJob();
        jd.setDescription(" ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(jd));

        assertEquals("Description is required", ex.getMessage());
    }

    @Test
    void save_withNullDescription_rejects() {
        JobDescription jd = validJob();
        jd.setDescription(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(jd));

        assertEquals("Description is required", ex.getMessage());
    }

    @Test
    void save_withTooLongDescription_rejects() {
        JobDescription jd = validJob();
        jd.setDescription(String.join(" ", java.util.Collections.nCopies(51, "word")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(jd));

        assertEquals("Job description must be between 10 and 50 words", ex.getMessage());
    }

    @Test
    void save_withMinGreaterThanMax_rejects() {
        JobDescription jd = validJob();
        jd.setExperienceMin(6);
        jd.setExperienceMax(3);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(jd));

        assertEquals("Experience min cannot be greater than max", ex.getMessage());
    }

    @Test
    void save_withMissingExperienceRange_rejects() {
        JobDescription jd = validJob();
        jd.setExperienceMin(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(jd));

        assertEquals("Experience min and max are required", ex.getMessage());
    }

    @Test
    void save_withNegativeExperience_rejects() {
        JobDescription jd = validJob();
        jd.setExperienceMin(-1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(jd));

        assertEquals("Experience cannot be negative", ex.getMessage());
    }

    @Test
    void save_withMissingSalaryRange_rejects() {
        JobDescription jd = validJob();
        jd.setSalaryMax(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(jd));

        assertEquals("Salary min and max are required", ex.getMessage());
    }

    @Test
    void save_withNegativeSalary_rejects() {
        JobDescription jd = validJob();
        jd.setSalaryMin(-1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(jd));

        assertEquals("Salary cannot be negative", ex.getMessage());
    }

    @Test
    void save_withNegativeSalaryMax_rejects() {
        JobDescription jd = validJob();
        jd.setSalaryMax(-1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(jd));

        assertEquals("Salary cannot be negative", ex.getMessage());
    }

    @Test
    void save_withSalaryMinGreaterThanMax_rejects() {
        JobDescription jd = validJob();
        jd.setSalaryMin(100);
        jd.setSalaryMax(50);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.save(jd));

        assertEquals("Salary min cannot be greater than max", ex.getMessage());
    }

    @Test
    void save_withExistingDerivedFields_keepsThem() {
        JobDescription jd = validJob();
        jd.setExperience(4);
        jd.setSalary("Custom salary");
        when(repo.save(jd)).thenReturn(jd);

        JobDescription saved = service.save(jd);

        assertEquals(4, saved.getExperience());
        assertEquals("Custom salary", saved.getSalary());
    }

    @Test
    void getAllAndGetByIdAndDelete_delegateToRepository() {
        JobDescription jd = validJob();
        when(repo.findAll()).thenReturn(List.of(jd));
        when(repo.findById(1L)).thenReturn(Optional.of(jd));

        assertEquals(List.of(jd), service.getAll());
        assertEquals(jd, service.getById(1L));
        service.delete(1L);

        verify(repo).deleteById(1L);
    }

    private JobDescription validJob() {
        JobDescription jd = new JobDescription();
        jd.setTitle("Java Developer");
        jd.setDescription("Build maintain test document deploy monitor improve support review Java services");
        jd.setExperienceMin(2);
        jd.setExperienceMax(5);
        jd.setSalaryMin(700000);
        jd.setSalaryMax(1200000);
        return jd;
    }
}

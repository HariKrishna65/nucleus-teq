package com.interview.tracker.repository;

import com.interview.tracker.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Interview entity.
 */
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    /**
     * Fetch interviews assigned to a panel.
     */
    List<Interview> findByPanel_Id(Long panelId);
}
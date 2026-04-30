package com.interview.tracker.repository;

import com.interview.tracker.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for candidate applications.
 */
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    
    List<Candidate> findByUser_Id(Long userId);

    Optional<Candidate> findByUser_IdAndJd_Id(Long userId, Long jdId);

    Optional<Candidate> findByPhone(String phone);

    List<Candidate> findAllByOrderByApplicationDateDesc();
}
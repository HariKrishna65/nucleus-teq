package com.interview.tracker.repository;

import com.interview.tracker.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByCandidateId(Long candidateId);
    List<Interview> findByStatus(Interview.Status status);
    List<Interview> findByRound(Interview.Round round);
}
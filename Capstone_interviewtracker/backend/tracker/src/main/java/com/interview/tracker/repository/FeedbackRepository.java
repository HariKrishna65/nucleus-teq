package com.interview.tracker.repository;

import com.interview.tracker.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByInterview_Candidate_IdOrderByIdDesc(Long candidateId);
    Optional<Feedback> findTopByInterview_IdOrderByIdDesc(Long interviewId);
    boolean existsByInterview_IdAndPanel_Id(Long interviewId, Long panelId);

    @Query("select count(distinct f.panel.id) from Feedback f where f.interview.id = :interviewId and f.panel.id is not null")
    long countDistinctPanelsByInterviewId(@Param("interviewId") Long interviewId);
}

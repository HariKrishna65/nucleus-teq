package com.interview.tracker.repository;

import com.interview.tracker.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByPanel_Id(Long panelId);

    List<Interview> findByCandidate_Id(Long candidateId);

    @Query("select distinct i from Interview i left join i.panels p where i.panel.id = :panelId or p.id = :panelId")
    List<Interview> findAssignedToPanel(@Param("panelId") Long panelId);
}
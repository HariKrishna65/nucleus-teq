package com.interview.tracker.repository;

import com.interview.tracker.entity.Panel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PanelRepository extends JpaRepository<Panel, Long> {
    Optional<Panel> findByEmail(String email);
}
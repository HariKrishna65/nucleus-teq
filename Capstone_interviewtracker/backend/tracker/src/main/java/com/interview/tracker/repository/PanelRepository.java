package com.interview.tracker.repository;

import com.interview.tracker.entity.Panel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PanelRepository extends JpaRepository<Panel, Long> {
}
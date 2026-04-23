package com.startup_service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.startup_service.Entity.StartupDocument;

public interface StartupDocumentRepository extends JpaRepository<StartupDocument, Long> {
    List<StartupDocument> findByStartupIdOrderByCreatedAtDesc(Long startupId);
}

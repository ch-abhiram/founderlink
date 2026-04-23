package com.startup_service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.startup_service.Entity.StartupUpdate;

public interface StartupUpdateRepository extends JpaRepository<StartupUpdate, Long> {
    List<StartupUpdate> findByStartupIdOrderByCreatedAtDesc(Long startupId);
}

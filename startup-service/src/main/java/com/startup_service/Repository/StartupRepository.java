package com.startup_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.startup_service.Entity.Startup;

public interface StartupRepository extends JpaRepository<Startup, Long>, JpaSpecificationExecutor<Startup> {
}

package com.auth_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auth_service.Entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
	
}

package com.auth_service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auth_service.Entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUserId(Long userId);
}
